package com.example.socialnetworkingbackend.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MediaResponseDto {

    private String publicId;
    private String secureUrl;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String playbackUrl;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String thumbnailUrl;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String postId;

    private String resourceType;
    private Long dataSize;
    private String format;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private UserSummaryDto author;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long height;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long width;

    private LocalDateTime createdAt;
}
