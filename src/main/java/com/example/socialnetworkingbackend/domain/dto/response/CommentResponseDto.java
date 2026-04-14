package com.example.socialnetworkingbackend.domain.dto.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponseDto {

    private Long id;
    private String content;
    private Long postId;
    private Long parentCommentId;
    private UserSummaryDto author;
    private List<CommentResponseDto> replies;
    private int replyCount;
    private LocalDateTime createdAt;
    private LocalDateTime lastModifiedAt;
}
