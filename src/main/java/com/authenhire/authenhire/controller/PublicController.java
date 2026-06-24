package com.authenhire.authenhire.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.authenhire.authenhire.repo.CompanyRepository;
import com.authenhire.authenhire.repo.JobRepository;
import com.authenhire.authenhire.repo.NotificationRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class PublicController {

    private final JobRepository jobRepository;
    private final CompanyRepository companyRepository;
    private final NotificationRepository notificationRepo;

    public PublicController(JobRepository jobRepository,
            CompanyRepository companyRepository,
            NotificationRepository notificationRepo) {
        this.jobRepository = jobRepository;
        this.companyRepository = companyRepository;
        this.notificationRepo = notificationRepo;
    }

    @GetMapping("/")
    public String home(Model model, HttpSession session) {

        model.addAttribute("trendingJobs", jobRepository.findTop6ByOrderByPostedAtDesc());
        model.addAttribute("verifiedCompanies", companyRepository.findByVerifiedTrue());

        if (session.getAttribute("userId") != null) {
            Long userId = Long.valueOf(session.getAttribute("userId").toString());

            model.addAttribute("unreadCount",
                    notificationRepo.countByUserIdAndIsReadFalse(userId));
        } else {
            model.addAttribute("unreadCount", 0);
        }

        return "index";
    }

    @GetMapping("/jobs")
    public String jobs(@RequestParam(required = false) String q,
            @RequestParam(required = false) String loc,
            @RequestParam(required = false) String type,
            Model model) {
        model.addAttribute("jobs", jobRepository.search(q, loc, type));
        model.addAttribute("q", q);
        model.addAttribute("loc", loc);
        model.addAttribute("type", type);
        return "jobs";
    }

    @GetMapping("/companies")
    public String companies(@RequestParam(required = false) String q, Model model) {
        if (q == null || q.isBlank()) {
            model.addAttribute("companies", companyRepository.findAll());
        } else {
            model.addAttribute("companies", companyRepository.findByNameContainingIgnoreCase(q));
        }
        model.addAttribute("q", q);
        return "companies";
    }

    @GetMapping("/companies/{id}")
    public String companyProfile(@PathVariable Long id, Model model) {
        var c = companyRepository.findById(id).orElse(null);
        if (c == null) {
            return "redirect:/companies";
        }
        model.addAttribute("company", c);
        return "company-profile";
    }
}
