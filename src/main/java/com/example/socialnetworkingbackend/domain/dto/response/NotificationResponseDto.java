package com.example.socialnetworkingbackend.domain.dto.response;

import com.example.socialnetworkingbackend.constant.NotificationType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotificationResponseDto {

    private Long id;
    private UserSummaryDto actor;
    private NotificationType notificationType;
    private String targetId;
    private String targetUrl;
    private String message;
    private Boolean isRead;
    private LocalDateTime createdAt;

}
