package com.example.socialnetworkingbackend.domain.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponseDto {

    private Long id;
    private String content;
    private Long postId;
    private Long parentCommentId;
    private Integer commentLevel;
    private UserSummaryDto author;
    private UserSummaryDto replyToUser;
    private int replyCount;
    private LocalDateTime createdAt;
    private LocalDateTime lastModifiedAt;
}
