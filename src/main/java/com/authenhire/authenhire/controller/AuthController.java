package com.authenhire.authenhire.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.authenhire.authenhire.entity.JobSeeker;
import com.authenhire.authenhire.repo.JobSeekerRepository;
import com.authenhire.authenhire.service.AuthService;

import jakarta.servlet.http.HttpSession;

@Controller
public class AuthController {

    private final AuthService authService;
    private final JobSeekerRepository jobSeekerRepository;

    public AuthController(AuthService authService, JobSeekerRepository jobSeekerRepository) {
        this.authService = authService;
        this.jobSeekerRepository = jobSeekerRepository;
    }

    // ---------------- LOGIN PAGE ----------------
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    // ---------------- SIGNUP CHOICE PAGE ----------------
    @GetMapping("/signup")
    public String signupChoicePage() {
        return "signup-choice";
    }

    // ---------------- JOB SEEKER SIGNUP PAGE ----------------
     @GetMapping("/signup/jobseeker")
    public String jobSeekerSignupPage(@RequestParam(defaultValue = "normal") String plan, Model model) {
        model.addAttribute("plan", plan.toLowerCase());
        return "signup";
    }                       

    // ---------------- COMPANY SIGNUP PAGE ----------------
    @GetMapping("/signup/company")
    public String companySignupPage() {
        return "company/signup";
    }

    // ---------------- LOGIN ----------------
    @PostMapping("/login")
    public String doLogin(@RequestParam String email,
            @RequestParam String password,
            HttpSession session,
            Model model) {

        return authService.loginAndGetUser(email, password)
                .map(user -> {

                    session.setAttribute("role", user.getRole());
                    session.setAttribute("userEmail", user.getEmail());
                    session.setAttribute("userId", user.getId());
                    session.setAttribute("companyId", user.getCompanyId());

                    // ✅ Add premium / normal session for job seeker
                    if ("JOB_SEEKER".equalsIgnoreCase(user.getRole())) {
                        JobSeeker js = jobSeekerRepository.findByUser(user).orElse(null);

                        if (js != null) {
                            session.setAttribute("jobSeekerPlan", js.getPlanType());
                            session.setAttribute("paymentStatus", js.getPaymentStatus());
                        } else {
                            session.setAttribute("jobSeekerPlan", "NORMAL");
                            session.setAttribute("paymentStatus", "FREE");
                        }

                        return "redirect:/";
                    }

                    if ("ADMIN".equalsIgnoreCase(user.getRole())) {
                        return "redirect:/admin/dashboard";
                    }

                    if ("COMPANY".equalsIgnoreCase(user.getRole())) {
                        return "redirect:/company/dashboard";
                    }

                    return "redirect:/";
                })
                .orElseGet(() -> {
                    model.addAttribute("error", "Invalid email or password!");
                    return "login";
                });
    }

    // ---------------- JOB SEEKER SIGNUP ----------------
    @PostMapping("/signup/jobseeker")
    public String jobSeekerSignup(@RequestParam String fullName,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String phone,
            @RequestParam String qualification,
            @RequestParam String planType,
            @RequestParam(required = false) String candidateType,
            @RequestParam(required = false) Integer experienceYears,
            @RequestParam(required = false) String preferredLocation,
            @RequestParam(required = false) String skills,
            @RequestParam(required = false) String paymentMethod,
            @RequestParam(required = false) MultipartFile resumeFile,
            Model model) {

        String err = authService.signupJobSeeker(
                fullName,
                email,
                password,
                phone,
                qualification,
                planType,
                candidateType,
                experienceYears,
                preferredLocation,
                skills,
                paymentMethod,
                resumeFile
        );

        if (err != null) {
            model.addAttribute("error", err);
            model.addAttribute("plan", planType != null ? planType.toLowerCase() : "normal");
            return "signup";
        }

        return "redirect:/login";
    }

    // ---------------- COMPANY SIGNUP ----------------
    @PostMapping("/signup/company")
    public String companySignup(@RequestParam String fullName,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String companyName,
            @RequestParam(required = false) String website,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String about,
            @RequestParam String gstNumber,
            @RequestParam String address,
            @RequestParam String pincode,
            @RequestParam String paymentMethod,
            @RequestParam(required = false) MultipartFile logoFile,
            @RequestParam(required = false) MultipartFile gstFile,
            @RequestParam(required = false) MultipartFile panFile,
            @RequestParam(required = false) MultipartFile incorporationFile,
            @RequestParam(required = false) MultipartFile addressProofFile,
            Model model) {

        String err = authService.signupCompany(
                fullName,
                email,
                password,
                companyName,
                website,
                location,
                about,
                gstNumber,
                address,
                pincode,
                paymentMethod,
                logoFile,
                gstFile,
                panFile,
                incorporationFile,
                addressProofFile
        );

        if (err != null) {
            model.addAttribute("error", err);
            return "company/signup";
        }

        return "redirect:/login";
    }

    // ---------------- LOGOUT ----------------
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}