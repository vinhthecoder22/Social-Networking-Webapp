package com.example.socialnetworkingbackend.domain.dto.request;

import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Getter
@Setter
public class RoleRequestDto {

    @NotBlank(message = "Role name must not be blank")
    @Pattern(regexp = "^ROLE_(ADMIN|USER)$", message = "Role must be ROLE_ADMIN or ROLE_USER")
    private String name;

}
