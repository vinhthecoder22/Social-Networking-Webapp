package com.example.socialnetworkingbackend.controller;

import com.example.socialnetworkingbackend.base.RestApiV1;
import com.example.socialnetworkingbackend.base.VsResponseUtil;
import com.example.socialnetworkingbackend.constant.ErrorMessage;
import com.example.socialnetworkingbackend.constant.SuccessMessage;
import com.example.socialnetworkingbackend.constant.UrlConstant;
import com.example.socialnetworkingbackend.domain.dto.request.ConfirmNewPasswordRequestDto;
import com.example.socialnetworkingbackend.domain.dto.response.VerifiedOtpResponseDto;
import com.example.socialnetworkingbackend.service.OtpForgotPasswordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.validation.Valid;

@RestApiV1
@RequiredArgsConstructor
@Log4j2
public class OtpForgotPasswordController {

    private final OtpForgotPasswordService otpForgotPasswordService;
    private final MessageSource messageSource;

    @PostMapping(value = UrlConstant.Auth.SEND_OTP)
    public ResponseEntity<?> sendOtp(@RequestParam("email") String receivedEmail) {
        boolean result = otpForgotPasswordService.sendOtpForgotPassword(receivedEmail);
        if (!result) {
            String message = messageSource.getMessage(ErrorMessage.OtpForgotPassword.ERR_SEND_FAILED, null,
                    LocaleContextHolder.getLocale());
            return VsResponseUtil.error(HttpStatus.BAD_REQUEST, message);
        }
        String message = messageSource.getMessage(SuccessMessage.ForgotPassword.SEND_OTP_SUCCESSFULLY, null,
                LocaleContextHolder.getLocale());
        return VsResponseUtil.successWithMessage(message);
    }

    @PostMapping(value = UrlConstant.Auth.VERIFY_OTP)
    public ResponseEntity<?> verifyOtp(@RequestParam("email") String email, @RequestParam("otpCode") String otpCode) {
        VerifiedOtpResponseDto response = otpForgotPasswordService.verifyOtpForgotPassword(email, otpCode);
        if (response == null) {
            String message = messageSource.getMessage(ErrorMessage.OtpForgotPassword.ERR_VERIFY_FAILED, null,
                    LocaleContextHolder.getLocale());
            return VsResponseUtil.error(HttpStatus.BAD_REQUEST, message);
        }
        return VsResponseUtil.success(response);
    }

    @PostMapping(value = UrlConstant.Auth.CHANGE_PASSWORD)
    public ResponseEntity<?> changePassword(@RequestBody @Valid ConfirmNewPasswordRequestDto requestDto) {
        otpForgotPasswordService.confirmChangeNewPassword(requestDto);
        String message = messageSource.getMessage(SuccessMessage.ForgotPassword.RESET_PASSWORD_SUCCESSFULLY, null,
                LocaleContextHolder.getLocale());
        return VsResponseUtil.successWithMessage(message);
    }
}
