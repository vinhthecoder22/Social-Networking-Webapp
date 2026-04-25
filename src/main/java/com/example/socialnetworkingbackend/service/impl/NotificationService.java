package com.example.socialnetworkingbackend.service.impl;

import com.example.socialnetworkingbackend.constant.ErrorMessage;
import com.example.socialnetworkingbackend.constant.NotificationType;
import com.example.socialnetworkingbackend.domain.dto.response.CursorPageResponse;
import com.example.socialnetworkingbackend.domain.dto.response.NotificationResponseDto;
import com.example.socialnetworkingbackend.domain.entity.Notification;
import com.example.socialnetworkingbackend.domain.entity.User;
import com.example.socialnetworkingbackend.domain.mapper.NotificationMapper;
import com.example.socialnetworkingbackend.exception.NotFoundException;
import com.example.socialnetworkingbackend.exception.UnauthorizedException;
import com.example.socialnetworkingbackend.repository.NotificationRepository;
import com.example.socialnetworkingbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationMapper notificationMapper;
    private final UserRepository userRepository;

    // Gửi thông báo & Push Real-time (WebSocket)
    @Async
    @Transactional
    public void sendNotification(User recipient, User actor, NotificationType notificationType, String targetId, String message) {
        log.info("Async Notification Thread started for user: {}", recipient.getUsername());

        Notification notification = Notification.builder()
                .recipient(recipient)
                .actor(actor)
                .notificationType(notificationType)
                .targetId(targetId)
                .message(message)
                .isRead(false)
                .build();

        Notification savedNotification = notificationRepository.save(notification);

        NotificationResponseDto responseDto = notificationMapper.toNotificationResponseDto(savedNotification);

        pushNotificationToWebSocket(recipient.getUsername(), responseDto);
    }

    private void pushNotificationToWebSocket(String username, NotificationResponseDto payload) {
        try {
            messagingTemplate.convertAndSendToUser(
                    username,
                    "/queue/notifications",
                    payload
            );
            log.info("WebSocket - Sent real-time notification to {}", username);
        } catch (Exception e) {
            log.error("WebSocket - Failed to send real-time notification to {}", username, e);
        }
    }

    // Lấy danh sách thông báo (Dùng Cursor Pagination để chống trôi dữ liệu)
    @Transactional(readOnly = true)
    public CursorPageResponse<NotificationResponseDto> getNotificationsCursor(String username, Long cursor, int size) {
        User recipient = userRepository.findByUsernameOrEmail(username, username)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.User.ERR_NOT_FOUND_USERNAME));

        // Lấy dư 1 phần tử để check xem còn trang tiếp theo không
        PageRequest pageRequest = PageRequest.of(0, size + 1);

        List<Notification> notifications = notificationRepository.findByRecipientIdWithCursor(
                recipient.getId(),
                cursor,
                pageRequest
        );

        boolean hasNext = notifications.size() > size;
        Long nextCursor = null;

        if (hasNext) {
            notifications.remove(notifications.size() - 1);
        }

        if (!notifications.isEmpty()) {
            nextCursor = notifications.get(notifications.size() - 1).getId();
        }

        List<NotificationResponseDto> data = notifications.stream()
                .map(notificationMapper::toNotificationResponseDto)
                .collect(Collectors.toList());

        return CursorPageResponse.<NotificationResponseDto>builder()
                .data(data)
                .nextCursor(nextCursor)
                .hasNext(hasNext)
                .build();
    }

    // Đánh dấu 1 thông báo là đã đọc
    @Transactional
    public void markAsRead(Long notificationId, String username) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.Notification.ERR_NOT_FOUND_ID));

        if (!notification.getRecipient().getUsername().equals(username)) {
            throw new UnauthorizedException(ErrorMessage.FORBIDDEN_UPDATE_DELETE);
        }

        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    // Đánh dấu tất cả thông báo là đã đọc
    @Transactional
    public void markAllAsRead(String username) {
        User user = userRepository.findByUsernameOrEmail(username, username)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.User.ERR_NOT_FOUND_USERNAME));

        notificationRepository.markAllAsRead(user.getId());
    }

    // Đếm số thông báo chưa đọc
    @Transactional(readOnly = true)
    public long getUnreadCount(String username) {
        User user = userRepository.findByUsernameOrEmail(username, username)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.User.ERR_NOT_FOUND_USERNAME));

        return notificationRepository.countByRecipientIdAndIsReadFalse(user.getId());
    }
}