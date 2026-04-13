package com.example.socialnetworkingbackend.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.example.socialnetworkingbackend.constant.AuthProvider;
import com.example.socialnetworkingbackend.constant.RoleConstant;
import com.example.socialnetworkingbackend.domain.entity.Role;
import com.example.socialnetworkingbackend.domain.entity.User;
import com.example.socialnetworkingbackend.repository.RoleRepository;
import com.example.socialnetworkingbackend.repository.UserRepository;
import com.example.socialnetworkingbackend.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class OAuth2ServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private OAuth2ServiceImpl oAuth2Service;

    private User mockUser;
    private Role mockRole;

    @BeforeEach
    void setUp() {
        mockRole = new Role();
        mockRole.setId(1L);
        mockRole.setName(RoleConstant.USER.toString());

        mockUser = new User();
        mockUser.setId("user-123");
        mockUser.setUsername("existing@gmail.com");
        mockUser.setEmail("existing@gmail.com");
        mockUser.setRole(mockRole);
    }

    @Test
    @DisplayName("Đăng nhập Google: User đã tồn tại trong DB -> Không tạo mới")
    void processUser_Google_ExistingUser() {
        // ARRANGE
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("email", "existing@gmail.com");
        attributes.put("sub", "google-id-123");

        OidcIdToken mockIdToken = mock(OidcIdToken.class);
        OidcUserInfo mockUserInfo = mock(OidcUserInfo.class);

        // Giả lập DB tìm thấy user
        when(userRepository.findByUsernameOrEmail("existing@gmail.com", "existing@gmail.com"))
                .thenReturn(Optional.of(mockUser));

        // ACT
        UserPrincipal principal = oAuth2Service.processUser("google", attributes, mockIdToken, mockUserInfo);

        // ASSERT
        assertNotNull(principal);
        assertEquals("existing@gmail.com", principal.getUsername());
        // Đảm bảo ko gọi hàm save() xuống DB vì user đã tồn tại
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Đăng nhập Facebook: User MỚI -> Tạo và lưu vào DB")
    void processUser_Facebook_NewUser() {
        // ARRANGE
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("email", "newuser@facebook.com");
        attributes.put("id", "fb-id-456");
        attributes.put("first_name", "Nguyen");
        attributes.put("last_name", "Vinh");

        // Giả lập DB ko tìm thấy user
        when(userRepository.findByUsernameOrEmail("newuser@facebook.com", "newuser@facebook.com"))
                .thenReturn(Optional.empty());
        // Giả lập tìm thấy Role USER
        when(roleRepository.findByName(RoleConstant.USER)).thenReturn(Optional.of(mockRole));

        // Giả lập hành vi Save: Trả về chính user được lưu (có gán thêm ID)
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            savedUser.setId("new-user-id");
            return savedUser;
        });

        // ACT (Facebook không có IdToken và UserInfo => truyền null)
        UserPrincipal principal = oAuth2Service.processUser("facebook", attributes, null, null);

        // ASSERT
        assertNotNull(principal);
        assertEquals("newuser@facebook.com", principal.getUsername());

        // Đảm bảo có gọi hàm save() xuống DB 1 lần để tạo user
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Test Fallback: Không có Email từ Provider -> Tự động sinh Email từ ID")
    void processUser_MissingEmail_FallbackToProviderId() {
        // ARRANGE
        Map<String, Object> attributes = new HashMap<>();
        // Cố tình ko truyền email vào map
        attributes.put("id", "secret-id-789");

        when(userRepository.findByUsernameOrEmail(anyString(), anyString())).thenReturn(Optional.empty());
        when(roleRepository.findByName(RoleConstant.USER)).thenReturn(Optional.of(mockRole));
        when(userRepository.save(any(User.class))).thenAnswer(i -> {
            User u = i.getArgument(0);
            u.setId("new-id");
            return u;
        });

        // ACT
        UserPrincipal principal = oAuth2Service.processUser("facebook", attributes, null, null);

        // ASSERT
        // Kỳ vọng email được sinh ra theo công thức: {providerId}@{registrationId}.com
        assertEquals("secret-id-789@facebook.com", principal.getUsername());
        verify(userRepository, times(1)).save(any(User.class));
    }
}