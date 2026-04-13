package com.example.socialnetworkingbackend.domain.dto.request;

import com.example.socialnetworkingbackend.constant.ErrorMessage;
import lombok.*;
import jakarta.validation.constraints.NotBlank;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReactionRequestDto {

    @NotBlank(message = ErrorMessage.NOT_BLANK_FIELD)
    private String reactionType;

}
