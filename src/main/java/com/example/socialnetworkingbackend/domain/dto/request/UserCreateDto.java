package com.example.socialnetworkingbackend.domain.dto.request;

import com.example.socialnetworkingbackend.constant.ErrorMessage;
import com.example.socialnetworkingbackend.constant.GenderConstant;
import com.example.socialnetworkingbackend.validation.annotation.ValidPassword;
import lombok.*;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserCreateDto {

    @NotBlank(message = ErrorMessage.INVALID_SOME_THING_FIELD_IS_REQUIRED)
    private String username;

    @NotBlank(message = ErrorMessage.INVALID_SOME_THING_FIELD_IS_REQUIRED)
    @ValidPassword(message = ErrorMessage.INVALID_FORMAT_PASSWORD)
    private String password;

    @NotBlank(message = ErrorMessage.NOT_BLANK_FIELD)
    private String firstName;

    @NotBlank(message = ErrorMessage.NOT_BLANK_FIELD)
    private String lastName;

    @NotBlank(message = ErrorMessage.NOT_BLANK_FIELD)
    @Email(message = ErrorMessage.INVALID_EMAIL)
    private String email;

    @NotNull(message = ErrorMessage.NOT_BLANK_FIELD)
    private GenderConstant gender;

    @NotNull(message = ErrorMessage.NOT_BLANK_FIELD)
    private LocalDate dob;
}