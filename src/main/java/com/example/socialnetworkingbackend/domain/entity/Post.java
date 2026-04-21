package com.example.socialnetworkingbackend.domain.entity;

import com.example.socialnetworkingbackend.constant.MediaType;
import com.example.socialnetworkingbackend.constant.PostStatusConstant;
import com.example.socialnetworkingbackend.domain.entity.common.FlagUserDateAuditing;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "post", indexes = {
        @Index(name = "idx_post_user_id", columnList = "user_id"),
        @Index(name = "idx_post_created_at", columnList = "created_at"),
        @Index(name = "idx_post_user_created", columnList = "user_id, created_at")
})
@SQLDelete(sql = "UPDATE post SET delete_flag = true WHERE id=? AND version=?")
@SQLRestriction("delete_flag = false")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Post extends FlagUserDateAuditing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "media_category_id", foreignKey = @ForeignKey(name = "FK_MEDIA_CATEGORY"))
    private PostCategory category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "original_post_id")
    private Post originalPost;

    @Column(name = "media_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private MediaType mediaType;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Media> mediaList = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Reaction> reactions = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    @Column(name = "reaction_count", nullable = false)
    private Long reactionCount = 0L;

    @Column(name = "comment_count", nullable = false)
    private Long commentCount = 0L;

    @Column(name = "share_count", nullable = false)
    private Long shareCount = 0L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PostStatusConstant status;

    // Cơ chế Optimistic Locking để chống sai số khi cộng dồn Like/Comment đồng thời
    @Version
    private Long version;
}
