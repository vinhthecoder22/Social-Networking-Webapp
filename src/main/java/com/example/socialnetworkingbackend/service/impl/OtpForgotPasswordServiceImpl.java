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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
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
    private final StringRedisTemplate stringRedisTemplate;

    @Value("${app.otp.expiry-minutes:5}")
    private int otpExpiryMinutes;

    @Value("${app.otp.max-attempts:5}")
    private int maxAttempts;

    @Value("${app.otp.lock-duration-minutes:15}")
    private int lockDurationMinutes;

    @Override
    public boolean sendOtpForgotPassword(String receivedEmail) {
        String lockKey = "otp_lock:" + receivedEmail;
        String otpKey = "otp:" + receivedEmail;

        // Nếu đang bị khóa vì nhập sai nhiều lần thì không cho gửi OTP mới
        if (redisService.hasKey(lockKey)) {
            throw new BadRequestException("Tài khoản đang bị khóa do nhập sai OTP nhiều lần. Vui lòng thử lại sau.");
        }
        // Chống spam gửi mail liên tục
        if (redisService.hasKey(otpKey)) {
            throw new BadRequestException(String.format("Vui lòng đợi %d phút trước khi yêu cầu mã OTP mới.", otpExpiryMinutes));
        }

        User user = userRepository.findByUsernameOrEmail(receivedEmail, receivedEmail)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.User.ERR_NOT_FOUND_EMAIL,
                        new String[]{receivedEmail}));

        String fullName = user.getFirstName() + " " + user.getLastName();
        String otpCode = generateOtpCode();

        Context context = new Context();
        context.setVariable("otpCode", otpCode);
        context.setVariable("fullName", fullName);

        log.info("Generating OTP for: {}", receivedEmail);
        String htmlContent = templateEngine.process("otp_send_email", context);
        mailService.sendEmailWithObject(receivedEmail, htmlContent, "Mã xác nhận quên mật khẩu");

        redisService.save(otpKey, otpCode, otpExpiryMinutes, TimeUnit.MINUTES);

        return true;
    }

    @Override
    public VerifiedOtpResponseDto verifyOtpForgotPassword(String email, String otpCode) {
        String lockKey = "otp_lock:" + email;
        String attemptKey = "otp_attempts:" + email;
        String otpKey = "otp:" + email;

        if (redisService.hasKey(lockKey)) {
            throw new BadRequestException("Tài khoản đã bị khóa. Vui lòng thử lại sau.");
        }

        String storedOtp = redisService.get(otpKey);

        if (storedOtp == null) {
            throw new BadRequestException(ErrorMessage.OtpForgotPassword.ERR_OTP_EXPIRED);
        }

        if (!storedOtp.equals(otpCode)) {
            Long attemptCount = stringRedisTemplate.opsForValue().increment(attemptKey);

            if (attemptCount != null && attemptCount == 1) {
                stringRedisTemplate.expire(attemptKey, otpExpiryMinutes, TimeUnit.MINUTES);
            }

            if (attemptCount != null && attemptCount >= maxAttempts) {
                redisService.save(lockKey, "locked", lockDurationMinutes, TimeUnit.MINUTES);
                redisService.delete(otpKey);
                redisService.delete(attemptKey);
                throw new BadRequestException("Bạn đã nhập sai quá " + maxAttempts + " lần. Vui lòng thử lại sau.");
            }

            throw new BadRequestException(ErrorMessage.OtpForgotPassword.ERR_VERIFY_FAILED);
        }

        redisService.delete(attemptKey);
        redisService.delete(otpKey);

        String resetPasswordToken = UUID.randomUUID().toString() + System.currentTimeMillis();
        redisService.save("reset_token:" + resetPasswordToken, email, otpExpiryMinutes, TimeUnit.MINUTES);

        log.info("Verified successfully otp code for email: {}", email);

        return VerifiedOtpResponseDto.builder()
                .resetPasswordToken(resetPasswordToken)
                .resetPasswordExpiryDate(LocalDateTime.now().plusMinutes(otpExpiryMinutes))
                .build();
    }

    @Override
    public boolean confirmChangeNewPassword(ConfirmNewPasswordRequestDto requestDto) {
        log.info("Confirming new password");
        String resetTokenKey = "reset_token:" + requestDto.getResetPasswordToken();
        String email = redisService.get(resetTokenKey);

        if (email == null) {
            throw new BadRequestException(ErrorMessage.OtpForgotPassword.ERR_CHANGE_PASSWORD_EXPIRED);
        }

        if (!requestDto.getNewPassword().equals(requestDto.getConfirmNewPassword())) {
            throw new BadRequestException(ErrorMessage.OtpForgotPassword.ERR_PASSWORD_NOT_MATCHED);
        }

        User user = userRepository.findByUsernameOrEmail(email, email)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.User.ERR_NOT_FOUND_EMAIL,
                        new String[]{email}));

        user.setPassword(passwordEncoder.encode(requestDto.getNewPassword()));
        userRepository.save(user);

        // Xóa token sau khi đổi mật khẩu thành công để tránh việc token bị tái sử dụng
        redisService.delete(resetTokenKey);

        log.info("Reset new password successfully for email: {}", email);
        return true;
    }

    private String generateOtpCode() {
        int otpCodeInt = 100000 + secureRandom.nextInt(900000);
        return String.valueOf(otpCodeInt);
    }

}