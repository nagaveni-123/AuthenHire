package com.authenhire.authenhire.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.authenhire.authenhire.repo.CompanyRepository;
import com.authenhire.authenhire.repo.CompanyVerificationRepository;
import com.authenhire.authenhire.service.CompanyVerificationService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final CompanyRepository companyRepository;
    private final CompanyVerificationRepository verificationRepository;
    private final CompanyVerificationService verificationService;

    public AdminController(CompanyRepository companyRepository,
            CompanyVerificationRepository verificationRepository,
            CompanyVerificationService verificationService) {
        this.companyRepository = companyRepository;
        this.verificationRepository = verificationRepository;
        this.verificationService = verificationService;
    }

    private boolean isAdmin(HttpSession session) {
        Object role = session.getAttribute("role");
        return role != null && role.toString().equalsIgnoreCase("ADMIN");
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        if (!isAdmin(session)) {
            return "redirect:/admin/login";
        }

        model.addAttribute("totalCompanies", companyRepository.count());
        model.addAttribute("pendingCompanies", companyRepository.countByAdminStatus("PENDING"));
        model.addAttribute("pendingVerifications", verificationRepository.countByAdminStatus("PENDING"));

        return "admin/dashboard";
    }

    @GetMapping("/companies")
    public String companies(HttpSession session,
            @RequestParam(required = false) String status,
            Model model) {
        if (!isAdmin(session)) {
            return "redirect:/admin/login";
        }

        if (status == null || status.isBlank() || status.equalsIgnoreCase("ALL")) {
            model.addAttribute("companies", companyRepository.findAll());
            model.addAttribute("status", "ALL");
        } else {
            model.addAttribute("companies", companyRepository.findByAdminStatus(status));
            model.addAttribute("status", status);
        }

        return "admin/companies";
    }

    @GetMapping("/companies/{id}")
    public String companyDetails(@PathVariable Long id, HttpSession session, Model model) {
        if (!isAdmin(session)) {
            return "redirect:/admin/login";
        }

        var company = companyRepository.findById(id).orElse(null);
        if (company == null) {
            return "redirect:/admin/companies";
        }

        model.addAttribute("company", company);
        model.addAttribute("verifications", verificationRepository.findByCompanyId(id));

        return "admin/company-details";
    }

    @GetMapping("/verifications")
    public String verifications(HttpSession session,
            @RequestParam(required = false) String status,
            Model model) {
        if (!isAdmin(session)) {
            return "redirect:/admin/login";
        }

        if (status == null || status.isBlank()) {
            status = "PENDING";
        }

        model.addAttribute("status", status);
        model.addAttribute("verifications", verificationRepository.findByAdminStatus(status));
        return "admin/verifications";
    }

    @PostMapping("/verifications/{id}/approve")
    public String approve(@PathVariable Long id, HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/admin/login";
        }
        verificationService.approveVerification(id);
        return "redirect:/admin/verifications?status=PENDING";
    }

    @PostMapping("/verifications/{id}/reject")
    public String reject(@PathVariable Long id,
            @RequestParam(required = false) String reason,
            HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/admin/login";
        }
        verificationService.rejectVerification(id);
        return "redirect:/admin/verifications?status=PENDING";
    }

    @GetMapping("/login")
    public String adminLoginPage() {
        return "admin/login";
    }

    @PostMapping("/login")
    public String adminLogin(@RequestParam String email,
            @RequestParam String password,
            HttpSession session,
            Model model) {

        if (email.equals("admin@authenhire.com") && password.equals("admin123")) {
            session.setAttribute("role", "ADMIN");
            return "redirect:/admin/dashboard";
        }
        model.addAttribute("error", "Invalid admin credentials");
        return "admin/login";
    }

    @GetMapping("/companies/{id}/edit")
    public String editCompany(@PathVariable Long id, HttpSession session, Model model) {
        if (!isAdmin(session)) {
            return "redirect:/admin/login";
        }

        var company = companyRepository.findById(id).orElse(null);
        if (company == null) {
            return "redirect:/admin/companies";
        }

        model.addAttribute("company", company);
        return "admin/company-edit";
    }

    @PostMapping("/companies/{id}/update")
    public String updateCompany(@PathVariable Long id,
            @RequestParam String name,
            @RequestParam(required = false) String website,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String about,
            @RequestParam(required = false) String logoUrl,
            @RequestParam(required = false) String aiStatus,
            @RequestParam(required = false) String adminStatus,
            @RequestParam(required = false) Boolean verified,
            HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/admin/login";
        }

        companyRepository.findById(id).ifPresent(c -> {
            c.setName(name);
            c.setWebsite(website);
            c.setLocation(location);
            c.setAbout(about);
            c.setLogoUrl(logoUrl);

            if (aiStatus != null && !aiStatus.isBlank()) {
                c.setAiStatus(aiStatus);
            }
            if (adminStatus != null && !adminStatus.isBlank()) {
                c.setAdminStatus(adminStatus);
            }
            if (verified != null) {
                c.setVerified(verified);
            }

            companyRepository.save(c);
        });

        return "redirect:/admin/companies/" + id;
    }

    @GetMapping("/logout")
    public String adminLogout(HttpSession session) {
        session.invalidate();
        return "redirect:/admin/login";
    }
}
