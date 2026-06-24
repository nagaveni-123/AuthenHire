package com.authenhire.authenhire.controller;

import java.time.LocalDateTime;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.authenhire.authenhire.entity.Application;
import com.authenhire.authenhire.entity.Company;
import com.authenhire.authenhire.entity.CompanyVerification;
import com.authenhire.authenhire.entity.Job;
import com.authenhire.authenhire.entity.Notification;
import com.authenhire.authenhire.entity.User;
import com.authenhire.authenhire.repo.ApplicationRepository;
import com.authenhire.authenhire.repo.CompanyRepository;
import com.authenhire.authenhire.repo.CompanyVerificationRepository;
import com.authenhire.authenhire.repo.JobRepository;
import com.authenhire.authenhire.repo.NotificationRepository;
import com.authenhire.authenhire.repo.UserRepository;
import com.authenhire.authenhire.service.CompanyVerificationService;
import com.authenhire.authenhire.service.FileStorageService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/company")
public class CompanyController {

    private final CompanyRepository companyRepo;
    private final CompanyVerificationRepository verificationRepo;
    private final FileStorageService fileStorage;
    private final UserRepository userRepo;
    private final JobRepository jobRepo;
    private final ApplicationRepository applicationRepo;
    private final NotificationRepository notificationRepo;
    private final CompanyVerificationService companyVerificationService;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public CompanyController(CompanyRepository companyRepo,
            CompanyVerificationRepository verificationRepo,
            FileStorageService fileStorage,
            UserRepository userRepo,
            JobRepository jobRepo,
            ApplicationRepository applicationRepo,
            NotificationRepository notificationRepo,
            CompanyVerificationService companyVerificationService) {
        this.companyRepo = companyRepo;
        this.verificationRepo = verificationRepo;
        this.fileStorage = fileStorage;
        this.userRepo = userRepo;
        this.jobRepo = jobRepo;
        this.applicationRepo = applicationRepo;
        this.notificationRepo = notificationRepo;
        this.companyVerificationService = companyVerificationService;
    }

    private boolean isCompany(HttpSession session) {
        Object role = session.getAttribute("role");
        return role != null && role.toString().equalsIgnoreCase("COMPANY");
    }

    private Company getCompany(HttpSession session) {
        Object companyIdObj = session.getAttribute("companyId");
        if (companyIdObj == null) {
            return null;
        }

        Long companyId = Long.valueOf(companyIdObj.toString());
        return companyRepo.findById(companyId).orElse(null);
    }

    @GetMapping("/login")
    public String loginPage() {
        return "company/login";
    }

    @PostMapping("/login")
    public String doLogin(@RequestParam String email,
            @RequestParam String password,
            HttpSession session,
            Model model) {

        var opt = userRepo.findByEmail(email);

        if (opt.isEmpty()) {
            model.addAttribute("error", "Invalid email or password");
            return "company/login";
        }

        User user = opt.get();

        if (user.getRole() == null || !user.getRole().equalsIgnoreCase("COMPANY")) {
            model.addAttribute("error", "Access denied");
            return "company/login";
        }

        if (user.getPasswordHash() == null || !encoder.matches(password, user.getPasswordHash())) {
            model.addAttribute("error", "Invalid email or password");
            return "company/login";
        }

        if (user.getCompanyId() == null) {
            model.addAttribute("error", "Company profile not linked. companyId missing for this user.");
            return "company/login";
        }

        session.setAttribute("role", "COMPANY");
        session.setAttribute("userId", user.getId());
        session.setAttribute("companyId", user.getCompanyId());

        return "redirect:/company/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        if (!isCompany(session)) {
            return "redirect:/company/login";
        }

        Company company = getCompany(session);
        if (company == null) {
            return "redirect:/company/login";
        }

        var latest = verificationRepo
                .findTopByCompanyIdOrderByCreatedAtDesc(company.getId())
                .orElse(null);

        model.addAttribute("company", company);
        model.addAttribute("verification", latest);

        return "company/dashboard";
    }

    @GetMapping("/profile")
    public String profile(HttpSession session, Model model) {
        if (!isCompany(session)) {
            return "redirect:/company/login";
        }

        Company company = getCompany(session);
        if (company == null) {
            return "redirect:/company/login";
        }

        model.addAttribute("company", company);
        return "company/profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(HttpSession session,
            @RequestParam String name,
            @RequestParam(required = false) String website,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String about,
            @RequestParam(required = false) String logoUrl,
            @RequestParam(required = false) String paymentMethod,
            Model model) {

        if (!isCompany(session)) {
            return "redirect:/company/login";
        }

        Company company = getCompany(session);
        if (company == null) {
            return "redirect:/company/login";
        }

        company.setName(name);
        company.setWebsite(website);
        company.setLocation(location);
        company.setAbout(about);
        company.setLogoUrl(logoUrl);

        if (paymentMethod != null && !paymentMethod.trim().isEmpty()) {
            company.setRegistrationPaymentMethod(paymentMethod);
            company.setRegistrationPaymentStatus("SUCCESS");
            company.setRegistrationFee(499.0);
        }

        companyRepo.save(company);

        model.addAttribute("success", "Company profile updated successfully.");
        return "redirect:/company/profile";
    }

    @GetMapping("/verification")
    public String verification(HttpSession session, Model model) {
        if (!isCompany(session)) {
            return "redirect:/company/login";
        }

        Company company = getCompany(session);
        if (company == null) {
            return "redirect:/company/login";
        }

        var latest = verificationRepo
                .findTopByCompanyIdOrderByCreatedAtDesc(company.getId())
                .orElse(null);

        model.addAttribute("company", company);
        model.addAttribute("verification", latest);

        return "company/verification";
    }

    @PostMapping("/verification/submit")
    public String submitVerification(HttpSession session,
            @RequestParam String gstNumber,
            @RequestParam String address,
            @RequestParam String pincode,
            @RequestParam(required = false) MultipartFile gstFile,
            @RequestParam(required = false) MultipartFile panFile,
            @RequestParam(required = false) MultipartFile incorporationFile,
            @RequestParam(required = false) MultipartFile addressProofFile,
            Model model) {

        if (!isCompany(session)) {
            return "redirect:/company/login";
        }

        Company company = getCompany(session);
        if (company == null) {
            return "redirect:/company/login";
        }

        Object userIdObj = session.getAttribute("userId");
        if (userIdObj == null) {
            model.addAttribute("error", "User session expired. Please login again.");
            model.addAttribute("company", company);
            return "company/verification";
        }

        Long userId = Long.valueOf(userIdObj.toString());

        try {
            String folder = "company_" + company.getId();

            CompanyVerification v = new CompanyVerification();
            v.setUserId(userId);
            v.setCompanyId(company.getId());
            v.setCompanyName(company.getName());
            v.setGstNumber(gstNumber);
            v.setAddress(address);
            v.setPincode(pincode);
            v.setCreatedAt(LocalDateTime.now());

            v.setGstFile(fileStorage.save(gstFile, folder));
            v.setPanFile(fileStorage.save(panFile, folder));
            v.setIncorporationFile(fileStorage.save(incorporationFile, folder));
            v.setAddressProofFile(fileStorage.save(addressProofFile, folder));

            System.out.println("CONTROLLER CALLED - SUBMITTING TO SERVICE");
            companyVerificationService.submitVerification(v);

            return "redirect:/company/verification";

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Upload failed: " + e.getMessage());
            model.addAttribute("company", company);
            return "company/verification";
        }
    }

    @GetMapping("/jobs")
    public String jobs(HttpSession session, Model model) {
        if (!isCompany(session)) {
            return "redirect:/company/login";
        }

        Company company = getCompany(session);
        if (company == null) {
            return "redirect:/company/login";
        }

        model.addAttribute("company", company);
        model.addAttribute("jobs", jobRepo.findByCompany_IdOrderByPostedAtDesc(company.getId()));
        return "company/jobs";
    }

    @GetMapping("/jobs/add")
    public String addJobPage(HttpSession session, Model model) {
        if (!isCompany(session)) {
            return "redirect:/company/login";
        }

        Company company = getCompany(session);
        if (company == null) {
            return "redirect:/company/login";
        }

        if (!company.isVerified()) {
            return "redirect:/company/verification";
        }

        model.addAttribute("company", company);
        return "company/job-add";
    }

    @PostMapping("/jobs/add")
    public String addJob(HttpSession session,
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String salary,
            @RequestParam String jobType,
            @RequestParam(required = false) String experience,
            @RequestParam(required = false) String paymentMethod,
            Model model) {

        if (!isCompany(session)) {
            return "redirect:/company/login";
        }

        Company company = getCompany(session);
        if (company == null) {
            return "redirect:/company/login";
        }

        if (!company.isVerified()) {
            return "redirect:/company/verification";
        }

        Job job = new Job();
        job.setTitle(title);
        job.setDescription(description);
        job.setLocation(location);
        job.setSalaryRange(salary);
        job.setJobType(jobType);
        job.setExperience(experience);
        job.setCompany(company);
        job.setPostedAt(LocalDateTime.now());

        jobRepo.save(job);

        return "redirect:/company/jobs";
    }

    @GetMapping({"/applications", "/applications/"})
    public String applications(HttpSession session, Model model) {
        if (!isCompany(session)) {
            return "redirect:/company/login";
        }

        Company company = getCompany(session);
        if (company == null) {
            return "redirect:/company/login";
        }

        model.addAttribute("applications", applicationRepo.findByJob_Company_Id(company.getId()));
        return "company/applications";
    }

    @PostMapping("/applications/{id}/approve")
    public String approve(@PathVariable Long id, HttpSession session) {
        if (!isCompany(session)) {
            return "redirect:/company/login";
        }

        Application app = applicationRepo.findById(id).orElse(null);
        if (app != null) {
            app.setStatus("APPROVED");
            applicationRepo.save(app);

            Notification n = new Notification();
            n.setUserId(app.getUser().getId());
            n.setMessage("Your application for '" + app.getJob().getTitle() + "' has been approved.");
            notificationRepo.save(n);
        }

        return "redirect:/company/applications";
    }

    @PostMapping("/applications/{id}/reject")
    public String reject(@PathVariable Long id,
            @RequestParam(required = false) String reason,
            HttpSession session) {
        if (!isCompany(session)) {
            return "redirect:/company/login";
        }

        Application app = applicationRepo.findById(id).orElse(null);
        if (app != null) {
            app.setStatus("REJECTED");
            app.setReason(reason);
            applicationRepo.save(app);

            Notification n = new Notification();
            n.setUserId(app.getUser().getId());
            n.setMessage("Your application for '" + app.getJob().getTitle() + "' has been rejected.");
            notificationRepo.save(n);
        }

        return "redirect:/company/applications";
    }
}
