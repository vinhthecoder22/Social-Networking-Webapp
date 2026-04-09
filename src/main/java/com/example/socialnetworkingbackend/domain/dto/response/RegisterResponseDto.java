package com.example.socialnetworkingbackend.domain.dto.response;

import com.example.socialnetworkingbackend.constant.GenderConstant;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RegisterResponseDto {

    private String username;
    private String email;
    private String lastName;
    private String firstName;
    private GenderConstant gender;
}
