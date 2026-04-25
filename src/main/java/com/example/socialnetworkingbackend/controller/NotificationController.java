package com.example.socialnetworkingbackend.controller;

import com.example.socialnetworkingbackend.base.RestApiV1;
import com.example.socialnetworkingbackend.base.VsResponseUtil;
import com.example.socialnetworkingbackend.constant.UrlConstant;
import com.example.socialnetworkingbackend.service.impl.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestApiV1
@RequiredArgsConstructor
@Tag(name = "Notification", description = "API quản lý thông báo người dùng")
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "Lấy danh sách thông báo cá nhân (Cursor Pagination)")
    @GetMapping(UrlConstant.Notification.GET_NOTIFICATIONS)
    public ResponseEntity<?> getNotifications(
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "20") int size,
            Principal principal) {
        return VsResponseUtil.success(notificationService.getNotificationsCursor(principal.getName(), cursor, size));
    }

    @Operation(summary = "Lấy số lượng thông báo chưa đọc")
    @GetMapping(UrlConstant.Notification.GET_UNREAD_COUNT)
    public ResponseEntity<?> getUnreadCount(Principal principal) {
        return VsResponseUtil.success(notificationService.getUnreadCount(principal.getName()));
    }

    @Operation(summary = "Đánh dấu một thông báo là đã đọc")
    @PatchMapping(UrlConstant.Notification.MARK_AS_READ)
    public ResponseEntity<?> markAsRead(
            @PathVariable("notificationId") Long notificationId,
            Principal principal) {
        notificationService.markAsRead(notificationId, principal.getName());
        return VsResponseUtil.success("Marked as read");
    }

    @Operation(summary = "Đánh dấu tất cả thông báo là đã đọc")
    @PatchMapping(UrlConstant.Notification.MARK_ALL_AS_READ)
    public ResponseEntity<?> markAllAsRead(Principal principal) {
        notificationService.markAllAsRead(principal.getName());
        return VsResponseUtil.success("All marked as read");
    }
}