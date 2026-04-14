package com.example.socialnetworkingbackend.domain.dto.request;

import com.example.socialnetworkingbackend.constant.ErrorMessage;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReplyCommentRequestDto {

    @NotNull(message = ErrorMessage.NOT_BLANK_FIELD)
    private Long parentCommentId;

    @NotBlank(message = ErrorMessage.NOT_BLANK_FIELD)
    private String content;

}
