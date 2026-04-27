package com.example.socialnetworkingbackend.service.impl;

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
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @InjectMocks
    private OtpForgotPasswordServiceImpl otpService;

    private static final String TEST_EMAIL = "test@gmail.com";
    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setFirstName("Nguyen");
        mockUser.setLastName("Vinh");
        mockUser.setEmail(TEST_EMAIL);

        ReflectionTestUtils.setField(otpService, "otpExpiryMinutes", 5);
        ReflectionTestUtils.setField(otpService, "maxAttempts", 5);
        ReflectionTestUtils.setField(otpService, "lockDurationMinutes", 15);
    }

    @Test
    @DisplayName("Send OTP succeeds when user exists and cooldown is clear")
    void sendOtpForgotPassword_Success() {
        when(redisService.hasKey("otp_lock:" + TEST_EMAIL)).thenReturn(false);
        when(redisService.hasKey("otp:" + TEST_EMAIL)).thenReturn(false);
        when(userRepository.findByUsernameOrEmail(TEST_EMAIL, TEST_EMAIL)).thenReturn(Optional.of(mockUser));
        when(templateEngine.process(eq("otp_send_email"), any(Context.class))).thenReturn("<html>Mocked HTML</html>");

        boolean result = otpService.sendOtpForgotPassword(TEST_EMAIL);

        assertTrue(result);
        verify(mailService, times(1)).sendEmailWithObject(
                eq(TEST_EMAIL),
                eq("<html>Mocked HTML</html>"),
                eq("Mã xác nhận quên mật khẩu"));
        verify(redisService, times(1)).save(
                eq("otp:" + TEST_EMAIL),
                anyString(),
                eq(5L),
                eq(TimeUnit.MINUTES));
    }

    @Test
    @DisplayName("Send OTP fails when cooldown is active")
    void sendOtpForgotPassword_Fail_SpamCooldown() {
        when(redisService.hasKey("otp_lock:" + TEST_EMAIL)).thenReturn(false);
        when(redisService.hasKey("otp:" + TEST_EMAIL)).thenReturn(true);

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> otpService.sendOtpForgotPassword(TEST_EMAIL));

        assertEquals("Vui lòng đợi 5 phút trước khi yêu cầu mã OTP mới.", exception.getMessage());
        verify(userRepository, never()).findByUsernameOrEmail(anyString(), anyString());
        verify(mailService, never()).sendEmailWithObject(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Send OTP fails when email does not exist")
    void sendOtpForgotPassword_Fail_EmailNotFound() {
        when(redisService.hasKey("otp_lock:" + TEST_EMAIL)).thenReturn(false);
        when(redisService.hasKey("otp:" + TEST_EMAIL)).thenReturn(false);
        when(userRepository.findByUsernameOrEmail(TEST_EMAIL, TEST_EMAIL)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> otpService.sendOtpForgotPassword(TEST_EMAIL));

        assertEquals(ErrorMessage.User.ERR_NOT_FOUND_EMAIL, exception.getMessage());
        verify(mailService, never()).sendEmailWithObject(anyString(), anyString(), anyString());
    }
}
