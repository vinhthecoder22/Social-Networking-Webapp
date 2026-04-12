package com.example.socialnetworkingbackend.domain.dto.request;

import com.example.socialnetworkingbackend.constant.ErrorMessage;
import com.example.socialnetworkingbackend.validation.annotation.FieldMatch;
import com.example.socialnetworkingbackend.validation.annotation.ValidPassword;
import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.NotBlank;

@Getter
@Setter
@FieldMatch(
        first = "newPassword",
        second = "confirmNewPassword",
        message = ErrorMessage.INVALID_PASSWORD
)
public class ChangePasswordRequestDto {

    @NotBlank(message = ErrorMessage.NOT_BLANK_FIELD)
    private String oldPassword;

    @NotBlank(message = ErrorMessage.NOT_BLANK_FIELD)
    @ValidPassword(message = ErrorMessage.INVALID_FORMAT_PASSWORD)
    private String newPassword;

    @NotBlank(message = ErrorMessage.NOT_BLANK_FIELD)
    private String confirmNewPassword;
}