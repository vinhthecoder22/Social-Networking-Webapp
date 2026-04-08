package com.example.socialnetworkingbackend.domain.entity;

import com.example.socialnetworkingbackend.constant.ReactionTypeConstant;
import com.example.socialnetworkingbackend.domain.entity.common.DateAuditing;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import jakarta.persistence.*;

@Entity
@Table(name = "reaction")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Reaction extends DateAuditing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ReactionTypeConstant reactionType;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @JoinColumn(name = "post_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Post post;

}

