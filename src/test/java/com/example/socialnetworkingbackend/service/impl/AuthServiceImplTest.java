package com.example.socialnetworkingbackend.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.example.socialnetworkingbackend.constant.ErrorMessage;
import com.example.socialnetworkingbackend.constant.RoleConstant;
import com.example.socialnetworkingbackend.domain.dto.request.RegisterRequestDto;
import com.example.socialnetworkingbackend.domain.dto.response.RegisterResponseDto;
import com.example.socialnetworkingbackend.domain.entity.Role;
import com.example.socialnetworkingbackend.domain.entity.User;
import com.example.socialnetworkingbackend.domain.mapper.UserMapper;
import com.example.socialnetworkingbackend.exception.ConflictException;
import com.example.socialnetworkingbackend.repository.RoleRepository;
import com.example.socialnetworkingbackend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private AuthServiceImpl authService;

    private RegisterRequestDto requestDto;
    private User user;
    private Role role;

    @BeforeEach
    void setUp() {
        requestDto = new RegisterRequestDto();
        requestDto.setUsername("testuser");
        requestDto.setEmail("test@gmail.com");
        requestDto.setPassword("Password@123");

        role = new Role();
        role.setName(RoleConstant.USER);

        user = new User();
        user.setUsername("testuser");
        user.setEmail("test@gmail.com");
    }

    @Test
    @DisplayName("Test Đăng ký thành công: Data hợp lệ, trả về Response DTO")
    void register_Success() {
        // ARRANGE
        // Khi Service gọi hàm check tồn tại, bắt Mock trả về false (chưa ai đăng ký)
        when(userRepository.existsByUsername(requestDto.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(requestDto.getEmail())).thenReturn(false);

        // Khi tìm Role USER, trả về role giả
        when(roleRepository.findByName(RoleConstant.USER)).thenReturn(Optional.of(role));

        // Khi mã hóa pass, trả về chuỗi đã mã hóa
        when(passwordEncoder.encode(requestDto.getPassword())).thenReturn("encodedPassword");

        // Khi lưu user xuống DB, trả về object user
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Chuẩn bị kết quả của Mapper
        RegisterResponseDto mockResponse = new RegisterResponseDto();
        mockResponse.setUsername("testuser");
        when(userMapper.toRegisterDto(any(User.class))).thenReturn(mockResponse);

        // ACT
        RegisterResponseDto result = authService.register(requestDto);

        // ASSERT
        assertNotNull(result); // Đảm bảo kết quả không bị null
        assertEquals("testuser", result.getUsername()); // Đảm bảo trả về đúng username

        // Đảm bảo hàm save của UserRepository đã thực sự được gọi đúng 1 lần
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Test Đăng ký thất bại: Trùng Username -> Ném ra ConflictException")
    void register_Fail_UsernameExists() {
        // ARRANGE
        // Cố tình ép Mock trả về true -> Báo là Username đã có người dùng
        when(userRepository.existsByUsername(requestDto.getUsername())).thenReturn(true);

        // ACT & ASSERT
        // Bắt lỗi xem service có ném đúng ngoại lệ ConflictException không
        ConflictException exception = assertThrows(ConflictException.class, () -> {
            authService.register(requestDto);
        });

        // Kiểm tra xem câu báo lỗi có chuẩn không
        assertEquals(ErrorMessage.Auth.ERR_ALREADY_EXISTS_USERNAME, exception.getMessage());

        // Đảm bảo khi có lỗi thì luồng bị ngắt, không bao h gọi hàm save()
        verify(userRepository, never()).save(any(User.class));
    }
}