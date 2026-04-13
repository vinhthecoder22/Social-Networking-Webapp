package com.example.socialnetworkingbackend.security.oauth2;

import com.example.socialnetworkingbackend.constant.UserStatus;
import com.example.socialnetworkingbackend.security.UserPrincipal;
import com.example.socialnetworkingbackend.security.jwt.JwtTokenProvider;
import com.example.socialnetworkingbackend.service.RedisService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final RedisService redisService;
    private final ObjectMapper objectMapper;

    private final String FRONTEND_URL = "http://localhost:3000";

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        // 1. Sinh Token
        String accessToken = jwtTokenProvider.generateToken(userPrincipal, false);
        String refreshToken = jwtTokenProvider.generateToken(userPrincipal, true);

        // 2. Lưu Refresh Token và Session vào Redis thay vì MySQL
        String username = userPrincipal.getUsername();
        redisService.save("refresh_token:" + username, refreshToken, 1440, TimeUnit.MINUTES);

        try {
            Map<String, Object> sessionData = new HashMap<>();
            sessionData.put("username", username);
            sessionData.put("status", UserStatus.ONLINE.name());
            sessionData.put("last_activity", LocalDateTime.now().toString());
            redisService.save("session:" + username, objectMapper.writeValueAsString(sessionData), 1440, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.error("Lỗi lưu session Redis: ", e);
        }

        // 3. Trả thẳng Token về URL của FE (FE sẽ bóc tách và cất vào LocalStorage)
        String targetUrl = String.format("%s/oauth2/redirect?accessToken=%s&refreshToken=%s", FRONTEND_URL, accessToken, refreshToken);
        response.sendRedirect(targetUrl);
    }
}