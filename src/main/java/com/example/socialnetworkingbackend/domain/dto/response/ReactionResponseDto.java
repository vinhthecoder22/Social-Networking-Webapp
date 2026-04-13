package com.example.socialnetworkingbackend.domain.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ReactionResponseDto {

    private Long id;
    private String reactionType;
    private Long postId;
    private String userId;

}
