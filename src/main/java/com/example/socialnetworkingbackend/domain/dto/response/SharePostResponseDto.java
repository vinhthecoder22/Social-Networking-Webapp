package com.example.socialnetworkingbackend.domain.dto.response;

import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SharePostResponseDto {

    private Long id;
    private String title;
    private String content;
    private String createdBy;
    private PostSummaryDto originalPost;

}
