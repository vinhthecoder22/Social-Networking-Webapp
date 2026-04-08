package com.example.socialnetworkingbackend.domain.entity;

import lombok.*;

import jakarta.persistence.*;

@Entity
@Table(name = "post_category")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PostCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "interaction_count", nullable = false, columnDefinition = "BIGINT DEFAULT 0")
    private long interactionCount;
}

