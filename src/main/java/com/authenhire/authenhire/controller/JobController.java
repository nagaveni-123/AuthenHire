package com.authenhire.authenhire.controller;

import java.time.LocalDateTime;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.authenhire.authenhire.entity.Application;
import com.authenhire.authenhire.entity.Job;
import com.authenhire.authenhire.entity.User;
import com.authenhire.authenhire.repo.ApplicationRepository;
import com.authenhire.authenhire.repo.JobRepository;
import com.authenhire.authenhire.repo.UserRepository;
import com.authenhire.authenhire.service.JobService;

import jakarta.servlet.http.HttpSession;

@Controller
public class JobController {

    private final JobService jobService;
    private final JobRepository jobRepo;
    private final UserRepository userRepo;
    private final ApplicationRepository applicationRepo;

    public JobController(JobService jobService,
            JobRepository jobRepo,
            UserRepository userRepo,
            ApplicationRepository applicationRepo) {
        this.jobService = jobService;
        this.jobRepo = jobRepo;
        this.userRepo = userRepo;
        this.applicationRepo = applicationRepo;
    }

    @GetMapping("/jobs/{id}")
    public String jobDetails(@PathVariable Long id, Model model) {
        return jobService.getJobById(id)
                .map(job -> {
                    model.addAttribute("job", job);
                    return "job-details";
                })
                .orElse("redirect:/");
    }

    @PostMapping("/jobs/{id}/apply")
    public String applyJob(@PathVariable Long id, HttpSession session) {

        Object userIdObj = session.getAttribute("userId");
        Object roleObj = session.getAttribute("role");

        if (userIdObj == null) {
            return "redirect:/login";
        }

        if (roleObj == null || !roleObj.toString().equalsIgnoreCase("JOB_SEEKER")) {
            return "redirect:/login";
        }

        Long userId = Long.valueOf(userIdObj.toString());

        User user = userRepo.findById(userId).orElse(null);
        Job job = jobRepo.findById(id).orElse(null);

        if (user == null || job == null) {
            return "redirect:/";
        }

        Application app = new Application();
        app.setUser(user);
        app.setJob(job);
        app.setStudentName(user.getFullName());
        app.setJobTitle(job.getTitle());
        app.setStatus("PENDING");
        app.setAppliedAt(LocalDateTime.now());

        applicationRepo.save(app);

        return "redirect:/jobs/" + id;
    }
}
