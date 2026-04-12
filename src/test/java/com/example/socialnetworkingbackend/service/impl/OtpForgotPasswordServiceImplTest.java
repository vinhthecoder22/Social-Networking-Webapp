package com.example.socialnetworkingbackend.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.example.socialnetworkingbackend.constant.ErrorMessage;
import com.example.socialnetworkingbackend.domain.entity.User;
import com.example.socialnetworkingbackend.exception.BadRequestException;
import com.example.socialnetworkingbackend.exception.NotFoundException;
import com.example.socialnetworkingbackend.repository.UserRepository;
import com.example.socialnetworkingbackend.service.MailService;
import com.example.socialnetworkingbackend.service.RedisService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@ExtendWith(MockitoExtension.class)
class OtpForgotPasswordServiceImplTest {

    @Mock
    private MailService mailService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TemplateEngine templateEngine;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private RedisService redisService;

    @InjectMocks
    private OtpForgotPasswordServiceImpl otpService;

    private final String TEST_EMAIL = "test@gmail.com";
    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setFirstName("Nguyen");
        mockUser.setLastName("Vinh");
        mockUser.setEmail(TEST_EMAIL);
    }

    @Test
    @DisplayName("Test Gửi OTP Thành Công: User hợp lệ, chưa bị khóa Spam")
    void sendOtpForgotPassword_Success() {
        // ARRANGE
        // 1. Giả lập Redis báo "chưa có key này" (Không bị spam)
        when(redisService.hasKey("otp:" + TEST_EMAIL)).thenReturn(false);

        // 2. Giả lập tìm thấy User trong DB
        when(userRepository.findByUsernameOrEmail(TEST_EMAIL, TEST_EMAIL)).thenReturn(Optional.of(mockUser));

        // 3. Giả lập Template Engine tạo file HTML thành công
        when(templateEngine.process(eq("otp_send_email"), any(Context.class))).thenReturn("<html>Mocked HTML</html>");

        // ACT
        boolean result = otpService.sendOtpForgotPassword(TEST_EMAIL);

        // ASSERT
        assertTrue(result, "Hàm phải trả về true khi gửi thành công");

        // Đảm bảo mailService đã thực sự được gọi 1 lần để gửi thư
        verify(mailService, times(1)).sendEmailWithObject(
                eq(TEST_EMAIL),
                eq("<html>Mocked HTML</html>"),
                eq("Mã xác nhận quên mật khẩu")
        );

        // Đảm bảo OTP đã được lưu vào Redis với TTL 5 phút
        verify(redisService, times(1)).save(startsWith("otp:" + TEST_EMAIL), anyString(), eq(5L), eq(TimeUnit.MINUTES));
    }

    @Test
    @DisplayName("Test Gửi OTP Thất Bại: Chặn Spam - Bắt lỗi BadRequestException")
    void sendOtpForgotPassword_Fail_SpamCooldown() {
        // ARRANGE
        // Giả lập Redis báo "Đang có key này rồi" (User vừa bấm xin mã cách đây ít phút)
        when(redisService.hasKey("otp:" + TEST_EMAIL)).thenReturn(true);

        // ACT & ASSERT
        // Kỳ vọng hàm sẽ văng lỗi BadRequestException
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            otpService.sendOtpForgotPassword(TEST_EMAIL);
        });

        assertEquals("Vui lòng đợi 5 phút trước khi yêu cầu mã OTP mới.", exception.getMessage());

        // Đảm bảo khi bị khóa Spam, hệ thống KHÔNG truy vấn DB và KHÔNG gửi mail
        verify(userRepository, never()).findByUsernameOrEmail(anyString(), anyString());
        verify(mailService, never()).sendEmailWithObject(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Test Gửi OTP Thất Bại: Không tìm thấy Email trong hệ thống")
    void sendOtpForgotPassword_Fail_EmailNotFound() {
        // ARRANGE
        when(redisService.hasKey("otp:" + TEST_EMAIL)).thenReturn(false); // Không bị spam
        // DB trả về rỗng (Không tìm thấy user)
        when(userRepository.findByUsernameOrEmail(TEST_EMAIL, TEST_EMAIL)).thenReturn(Optional.empty());

        // ACT & ASSERT
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            otpService.sendOtpForgotPassword(TEST_EMAIL);
        });

        assertEquals(ErrorMessage.User.ERR_NOT_FOUND_EMAIL, exception.getMessage());

        // Đảm bảo không có mail nào bị gửi đi
        verify(mailService, never()).sendEmailWithObject(anyString(), anyString(), anyString());
    }
}