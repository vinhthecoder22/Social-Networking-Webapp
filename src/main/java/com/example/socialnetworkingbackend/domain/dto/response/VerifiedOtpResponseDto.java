package com.example.socialnetworkingbackend.domain.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VerifiedOtpResponseDto {

    private String resetPasswordToken;
    private LocalDateTime resetPasswordExpiryDate;
}
