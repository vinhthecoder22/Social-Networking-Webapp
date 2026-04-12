package com.example.socialnetworkingbackend.service;

import com.example.socialnetworkingbackend.domain.dto.request.ConfirmNewPasswordRequestDto;
import com.example.socialnetworkingbackend.domain.dto.response.VerifiedOtpResponseDto;

public interface OtpForgotPasswordService {

    public boolean sendOtpForgotPassword(String receivedEmail);
    public VerifiedOtpResponseDto verifyOtpForgotPassword(String otp);
    public boolean confirmChangeNewPassword(ConfirmNewPasswordRequestDto requestDto);
}
