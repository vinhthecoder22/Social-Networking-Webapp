package com.example.socialnetworkingbackend.service.impl;

import com.example.socialnetworkingbackend.constant.NotificationType;
import com.example.socialnetworkingbackend.domain.dto.response.NotificationResponseDto;
import com.example.socialnetworkingbackend.domain.entity.Notification;
import com.example.socialnetworkingbackend.domain.entity.User;
import com.example.socialnetworkingbackend.domain.mapper.NotificationMapper;
import com.example.socialnetworkingbackend.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationMapper notificationMapper;

    @Transactional
    public void sendNotification(User recipient, User actor, NotificationType notificationType, String targetId, String message) {
        // Lưu vào Database (Đồng bộ)
        Notification notification = Notification.builder()
                .recipient(recipient)
                .actor(actor)
                .notificationType(notificationType)
                .targetId(targetId)
                .isRead(false)
                .build();

        Notification savedNotification = notificationRepository.save(notification);

        // Map sang DTO
        NotificationResponseDto responseDto = notificationMapper.toNotificationResponseDto(savedNotification);
        responseDto.setMessage(message);

        // Đẩy sang luồng chạy ngầm để không làm nghẽn API chính
        pushNotificationToWebSocket(recipient.getUsername(), responseDto);
    }

    // @Async đẩy hàm này ra một luồng ThreadPool khác chạy độc lập
    @Async
    public void pushNotificationToWebSocket(String username, NotificationResponseDto payload) {
        try {
            messagingTemplate.convertAndSendToUser(
                    username,
                    "/queue/notifications",
                    payload
            );
            log.info("Async - Sent real-time notification to {}", username);
        } catch (Exception e) {
            log.error("Async - Failed to send WebSocket notification to {}", username, e);
        }
    }
}