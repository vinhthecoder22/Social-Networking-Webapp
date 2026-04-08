package com.example.socialnetworkingbackend.domain.entity;

import com.example.socialnetworkingbackend.domain.entity.common.DateAuditing;
import lombok.*;

import jakarta.persistence.*;

@Entity
@Table(name = "notification")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Notification extends DateAuditing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient; // Người nhận thông báo

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id", nullable = false)
    private User actor; // Người tương tác (ai like, ai comment)

    @Column(nullable = false)
    private String notificationType; // LIKE, COMMENT, FOLLOW, MENTION...

    @Column(name = "target_id", nullable = false)
    private String targetId; // ID của post, comment hoặc user liên quan

    @Column(name = "target_url")
    private String targetUrl; // Link điều hướng khi click

    @Column(name = "is_read", nullable = false)
    private Boolean isRead = Boolean.FALSE;
}
