package com.authenhire.authenhire.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.authenhire.authenhire.repo.NotificationRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class NotificationController {

    private final NotificationRepository notificationRepo;

    public NotificationController(NotificationRepository notificationRepo) {
        this.notificationRepo = notificationRepo;
    }

    @GetMapping("/jobseeker/notifications")
    public String notifications(HttpSession session, Model model) {
        Object userIdObj = session.getAttribute("userId");

        if (userIdObj == null) {
            return "redirect:/login";
        }

        Long userId = Long.valueOf(userIdObj.toString());

        model.addAttribute("notifications",
                notificationRepo.findByUserIdOrderByCreatedAtDesc(userId));

        model.addAttribute("unreadCount",
                notificationRepo.countByUserIdAndIsReadFalse(userId));

        return "notifications";
    }
}
