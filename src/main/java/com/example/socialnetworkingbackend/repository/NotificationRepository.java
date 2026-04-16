package com.example.socialnetworkingbackend.repository;

import com.example.socialnetworkingbackend.domain.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
}
