package com.example.socialnetworkingbackend.domain.entity;

import com.cloudinary.utils.StringUtils;
import com.example.socialnetworkingbackend.constant.UploadStatusConstant;
import com.example.socialnetworkingbackend.domain.entity.common.DateAuditing;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "media")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Media extends DateAuditing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "singer_name")
    private String singerName;

    @Column(name = "public_id")
    private String publicId;

    @Column(name = "secure_url")
    private String secureUrl;

    @Column(name = "playback_url")
    private String playbackUrl;

    @Column(name = "resource_type", nullable = false)
    private String resourceType;

    @Column(name = "thumbnail_url")
    private String thumbnailUrl;

    @Column(name = "format")
    private String format;

    @Column(name = "data_size", nullable = false)
    private Long dataSize;

    private Long height;

    private Long width;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "FK_MEDIA_USER"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnore
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "FK_MEDIA_POT"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Post post;

    @Column(name = "status_upload")
    @Enumerated(EnumType.STRING)
    private UploadStatusConstant status;
}
