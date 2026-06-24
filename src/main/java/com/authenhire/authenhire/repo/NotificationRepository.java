package com.authenhire.authenhire.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.authenhire.authenhire.entity.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);

    long countByUserIdAndIsReadFalse(Long userId);
}