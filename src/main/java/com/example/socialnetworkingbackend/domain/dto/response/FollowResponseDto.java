package com.example.socialnetworkingbackend.domain.dto.response;

import lombok.*;
import java.time.LocalDateTime;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FollowResponseDto {

    private Long id;
    private String followerId;
    private String followingId;
    private LocalDateTime createdAt;

}
