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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

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

    @Value("${app.oauth2.redirect-uri:http://localhost:5173/oauth2-redirect}")
    private String frontendRedirectUri;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        String accessToken = jwtTokenProvider.generateToken(userPrincipal, false);
        String refreshToken = jwtTokenProvider.generateToken(userPrincipal, true);
        long refreshExpiry = jwtTokenProvider.getExpirationTimeRefresh();
        String username = userPrincipal.getUsername();

        redisService.saveRefreshToken(userPrincipal.getId(), refreshToken, refreshExpiry, TimeUnit.MILLISECONDS);

        try {
            Map<String, Object> sessionData = new HashMap<>();
            sessionData.put("username", username);
            sessionData.put("status", UserStatus.ONLINE.name());
            sessionData.put("last_activity", LocalDateTime.now().toString());
            redisService.save("session:" + username,
                    objectMapper.writeValueAsString(sessionData),
                    refreshExpiry,
                    TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.error("Failed to persist OAuth2 session", e);
        }

        String targetUrl = UriComponentsBuilder.fromUriString(frontendRedirectUri)
                .queryParam("accessToken", accessToken)
                .queryParam("refreshToken", refreshToken)
                .build()
                .toUriString();
        response.sendRedirect(targetUrl);
    }
}
