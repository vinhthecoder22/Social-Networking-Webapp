package com.example.socialnetworkingbackend.service.impl;

import com.example.socialnetworkingbackend.constant.ErrorMessage;
import com.example.socialnetworkingbackend.domain.dto.request.ConfirmNewPasswordRequestDto;
import com.example.socialnetworkingbackend.domain.dto.response.VerifiedOtpResponseDto;
import com.example.socialnetworkingbackend.domain.entity.User;
import com.example.socialnetworkingbackend.exception.BadRequestException;
import com.example.socialnetworkingbackend.exception.NotFoundException;
import com.example.socialnetworkingbackend.repository.UserRepository;
import com.example.socialnetworkingbackend.service.MailService;
import com.example.socialnetworkingbackend.service.OtpForgotPasswordService;
import com.example.socialnetworkingbackend.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Log4j2
public class OtpForgotPasswordServiceImpl implements OtpForgotPasswordService {

    private final MailService mailService;
    private final SecureRandom secureRandom = new SecureRandom();
    private final UserRepository userRepository;
    private final TemplateEngine templateEngine;
    private final PasswordEncoder passwordEncoder;
    private final RedisService redisService;

    @Override
    public boolean sendOtpForgotPassword(String receivedEmail) {
        if (redisService.hasKey("otp:" + receivedEmail)) {
            throw new BadRequestException("Vui lòng đợi 5 phút trước khi yêu cầu mã OTP mới.");
        }

        User user = userRepository.findByUsernameOrEmail(receivedEmail, receivedEmail)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.User.ERR_NOT_FOUND_EMAIL,
                        new String[] { receivedEmail }));

        String fullName = user.getFirstName() + " " + user.getLastName();
        String otpCode = generateOtpCode();

        Context context = new Context();
        context.setVariable("otpCode", otpCode);
        context.setVariable("fullName", fullName);

        log.info("Generating OTP for: {}", receivedEmail);
        String htmlContent = templateEngine.process("otp_send_email", context);
        mailService.sendEmailWithObject(receivedEmail, htmlContent, "Mã xác nhận quên mật khẩu");

        redisService.save("otp:" + receivedEmail, otpCode, 5, TimeUnit.MINUTES);

        return true;
    }

    @Override
    public VerifiedOtpResponseDto verifyOtpForgotPassword(String email, String otpCode) {
        String storedOtp = redisService.get("otp:" + email);

        if (storedOtp == null) {
            throw new BadRequestException(ErrorMessage.OtpForgotPassword.ERR_OTP_EXPIRED);
        }

        if (!storedOtp.equals(otpCode)) {
            throw new BadRequestException(ErrorMessage.OtpForgotPassword.ERR_VERIFY_FAILED);
        }

        String resetPasswordToken = UUID.randomUUID().toString() + System.currentTimeMillis();

        redisService.save("reset_token:" + resetPasswordToken, email, 5, TimeUnit.MINUTES);

        redisService.delete("otp:" + email);

        log.info("Verified successfully otp code for email: {}", email);
        return VerifiedOtpResponseDto.builder()
                .resetPasswordToken(resetPasswordToken)
                .resetPasswordExpiryDate(LocalDateTime.now().plusMinutes(5))
                .build();
    }

    @Override
    public boolean confirmChangeNewPassword(ConfirmNewPasswordRequestDto requestDto) {
        log.info("Confirming new password");
        String email = redisService.get("reset_token:" + requestDto.getResetPasswordToken());

        if (email == null) {
            throw new BadRequestException(ErrorMessage.OtpForgotPassword.ERR_CHANGE_PASSWORD_EXPIRED);
        }

        if (!requestDto.getNewPassword().equals(requestDto.getConfirmNewPassword())) {
            throw new BadRequestException(ErrorMessage.OtpForgotPassword.ERR_PASSWORD_NOT_MATCHED);
        }

        User user = userRepository.findByUsernameOrEmail(email, email)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.User.ERR_NOT_FOUND_EMAIL,
                        new String[] { email }));

        user.setPassword(passwordEncoder.encode(requestDto.getNewPassword()));
        userRepository.save(user);

        redisService.delete("reset_token:" + requestDto.getResetPasswordToken());

        log.info("Reset new password successfully");
        return true;
    }

    private String generateOtpCode() {
        int otpCodeInt = 100000 + secureRandom.nextInt(900000);
        return String.valueOf(otpCodeInt);
    }

}
