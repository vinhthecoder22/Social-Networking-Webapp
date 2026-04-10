package com.example.socialnetworkingbackend.domain.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostSummaryDto {
    private Long id;
    private String title;
    private String content;
    private List<MediaResponseDto> mediaList;
    private UserSummaryDto createdBy;
    private Long shareCount;
}