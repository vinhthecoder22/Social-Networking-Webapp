package com.example.socialnetworkingbackend.domain.entity;

import com.example.socialnetworkingbackend.domain.entity.common.UserDateAuditing;
import lombok.*;

import jakarta.persistence.*;

@Entity
@Table(name = "report")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Report extends UserDateAuditing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_post_id")
    private Post reportedPost; // Null nếu là report user

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_user_id")
    private User reportedUser; // Null nếu là report bài viết

    @Column(nullable = false)
    private String reason;

    @Column(nullable = false)
    private String status; // PENDING, RESOLVED, REJECTED
}
