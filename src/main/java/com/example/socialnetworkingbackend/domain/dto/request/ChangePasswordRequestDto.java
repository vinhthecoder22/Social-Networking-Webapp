package com.example.socialnetworkingbackend.domain.dto.request;

import com.example.socialnetworkingbackend.constant.ErrorMessage;
import com.example.socialnetworkingbackend.validation.annotation.ValidPassword;
import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.NotBlank;

@Getter
@Setter
public class ChangePasswordRequestDto {

    @NotBlank(message = ErrorMessage.NOT_BLANK_FIELD)
    private String oldPassword;

    @NotBlank(message = ErrorMessage.NOT_BLANK_FIELD)
    @ValidPassword(message = ErrorMessage.INVALID_FORMAT_PASSWORD)
    private String newPassword;
}