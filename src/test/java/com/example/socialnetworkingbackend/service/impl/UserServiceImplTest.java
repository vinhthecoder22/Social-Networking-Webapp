package com.example.socialnetworkingbackend.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.example.socialnetworkingbackend.constant.ErrorMessage;
import com.example.socialnetworkingbackend.domain.dto.request.ChangePasswordRequestDto;
import com.example.socialnetworkingbackend.domain.entity.User;
import com.example.socialnetworkingbackend.exception.BadRequestException;
import com.example.socialnetworkingbackend.repository.UserRepository;
import com.example.socialnetworkingbackend.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private UserPrincipal mockPrincipal;
    private User mockUser;
    private ChangePasswordRequestDto requestDto;

    @BeforeEach
    void setUp() {
        mockPrincipal = new UserPrincipal(
                "user-id-123",
                "Nguyen",
                "Vinh",
                "testuser",
                "encodedOldPassword",
                new ArrayList<>()
        );

        mockUser = new User();
        mockUser.setId("user-id-123");
        mockUser.setPassword("encodedOldPassword");

        requestDto = new ChangePasswordRequestDto();
        requestDto.setOldPassword("OldPassword@123");
        requestDto.setNewPassword("NewPassword@123");
        requestDto.setConfirmNewPassword("NewPassword@123");
    }

    @Test
    @DisplayName("Đổi mật khẩu thành công")
    void changePassword_Success() {
        // ARRANGE
        when(userRepository.findById("user-id-123")).thenReturn(Optional.of(mockUser));
        // Mật khẩu cũ nhập vào khớp với pass trong DB
        when(passwordEncoder.matches("OldPassword@123", "encodedOldPassword")).thenReturn(true);
        // Mã hóa mật khẩu mới
        when(passwordEncoder.encode("NewPassword@123")).thenReturn("encodedNewPassword");

        // ACT
        userService.changePassword(mockPrincipal, requestDto);

        // ASSERT
        assertEquals("encodedNewPassword", mockUser.getPassword());
        verify(userRepository, times(1)).save(mockUser);
    }

    @Test
    @DisplayName("Đổi mật khẩu thất bại: Nhập sai mật khẩu cũ")
    void changePassword_Fail_WrongOldPassword() {
        // ARRANGE
        when(userRepository.findById("user-id-123")).thenReturn(Optional.of(mockUser));
        // Mật khẩu cũ nhập vào không khớp
        when(passwordEncoder.matches("OldPassword@123", "encodedOldPassword")).thenReturn(false);

        // ACT & ASSERT
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            userService.changePassword(mockPrincipal, requestDto);
        });

        assertEquals(ErrorMessage.OtpForgotPassword.ERR_OLD_PASSWORD_INCORRECT, exception.getMessage());
        // Đảm bảo không có lệnh save() nào chạy xuống DB
        verify(userRepository, never()).save(any(User.class));
    }
}