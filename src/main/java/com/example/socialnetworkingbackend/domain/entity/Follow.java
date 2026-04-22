package com.example.socialnetworkingbackend.domain.entity;

import com.example.socialnetworkingbackend.domain.entity.common.DateAuditing;
import lombok.*;

import jakarta.persistence.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "follow", indexes = {
        @Index(name = "idx_follow_follower", columnList = "follower_id, following_id")
}, uniqueConstraints = {
        @UniqueConstraint(name = "UK_follow_follower_following", columnNames = {"follower_id", "following_id"})
})
public class Follow extends DateAuditing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "following_id", nullable = false)
    private User following;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "follower_id", nullable = false)
    private User follower;

}

