package com.example.socialnetworkingbackend.service.impl;

import com.example.socialnetworkingbackend.constant.ErrorMessage;
import com.example.socialnetworkingbackend.constant.RoleConstant;
import com.example.socialnetworkingbackend.domain.dto.request.LoginRequestDto;
import com.example.socialnetworkingbackend.domain.dto.request.RegisterRequestDto;
import com.example.socialnetworkingbackend.domain.dto.request.TokenRefreshRequestDto;
import com.example.socialnetworkingbackend.domain.dto.response.CommonResponseDto;
import com.example.socialnetworkingbackend.domain.dto.response.LoginResponseDto;
import com.example.socialnetworkingbackend.domain.dto.response.RegisterResponseDto;
import com.example.socialnetworkingbackend.domain.dto.response.TokenRefreshResponseDto;
import com.example.socialnetworkingbackend.domain.entity.User;
import com.example.socialnetworkingbackend.domain.mapper.UserMapper;
import com.example.socialnetworkingbackend.exception.ConflictException;
import com.example.socialnetworkingbackend.exception.NotFoundException;
import com.example.socialnetworkingbackend.exception.UnauthorizedException;
import com.example.socialnetworkingbackend.repository.RoleRepository;
import com.example.socialnetworkingbackend.repository.UserRepository;
import com.example.socialnetworkingbackend.security.UserPrincipal;
import com.example.socialnetworkingbackend.security.jwt.JwtTokenProvider;
import com.example.socialnetworkingbackend.service.AuthService;
import com.example.socialnetworkingbackend.service.RedisService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final RedisService redisService;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public RegisterResponseDto register(RegisterRequestDto req) {
        if (userRepository.existsByUsername(req.getUsername())) {
            throw new ConflictException(ErrorMessage.Auth.ERR_ALREADY_EXISTS_USERNAME);
        }
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new ConflictException(ErrorMessage.Auth.ERR_ALREADY_EXISTS_EMAIL);
        }

        User user = new User();
        user.setUsername(req.getUsername());
        user.setEmail(req.getEmail());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setFirstName(req.getFirstName());
        user.setLastName(req.getLastName());
        user.setDob(req.getDob());
        user.setGender(req.getGender());
        user.setRole(roleRepository.findByName(RoleConstant.USER)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.Role.ERR_NOT_FOUND, new String[] { RoleConstant.USER })));

        return userMapper.toRegisterDto(userRepository.save(user));
    }

    @Override
    public LoginResponseDto login(LoginRequestDto request, HttpServletRequest httpServletRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsernameOrEmail(), request.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            String accessToken = jwtTokenProvider.generateToken(userPrincipal, false);
            String refreshToken = jwtTokenProvider.generateToken(userPrincipal, true);

            // Stateless: No DB session creation.

            return new LoginResponseDto(accessToken, refreshToken, userPrincipal.getId(), authentication.getAuthorities());
        } catch (InternalAuthenticationServiceException e) {
            throw new UnauthorizedException(ErrorMessage.Auth.ERR_INCORRECT_USERNAME);
        } catch (BadCredentialsException e) {
            throw new UnauthorizedException(ErrorMessage.Auth.ERR_INCORRECT_PASSWORD);
        }
    }

    @Override
    public TokenRefreshResponseDto refresh(TokenRefreshRequestDto request) {
        logger.info("Processing refresh token request");

        String refreshToken = request.getRefreshToken();
        if (!StringUtils.hasText(refreshToken)) {
            logger.error("Empty refresh token");
            throw new UnauthorizedException(ErrorMessage.Auth.INVALID_REFRESH_TOKEN);
        }

        if (redisService.hasKey("blacklist:" + refreshToken)) {
            throw new UnauthorizedException(ErrorMessage.Auth.INVALID_REFRESH_TOKEN);
        }

        try {
            if (jwtTokenProvider.validateToken(refreshToken)) {
                String username = jwtTokenProvider.extractClaimUsername(refreshToken);
                UserPrincipal userPrincipal = (UserPrincipal) userDetailsService.loadUserByUsername(username);
                String newAccessToken = jwtTokenProvider.generateToken(userPrincipal, false);

                // No DB session check

                logger.info("Refresh token successful for user: {}", username);
                return new TokenRefreshResponseDto(newAccessToken, refreshToken);
            }
        } catch (Exception e) {
            logger.error("Error processing refresh token: {}", refreshToken, e);
            throw new UnauthorizedException(ErrorMessage.Auth.ERR_REFRESH_TOKEN);
        }
        return null;
    }

    @Override
    public CommonResponseDto logout(HttpServletRequest request) {
        logger.info("Processing logout request");
        String bearerToken = request.getHeader("Authorization");
        if (!StringUtils.hasText(bearerToken) || !bearerToken.startsWith("Bearer ")) {
            throw new UnauthorizedException(ErrorMessage.Auth.INVALID_ACCESS_TOKEN);
        }
        String token = bearerToken.substring(7);
        logger.info("Logout token: {}", token);

        long accessTokenExpiry = jwtTokenProvider.getExpirationTimeAccess();

        redisService.save("blacklist:" + token, "logout", accessTokenExpiry, TimeUnit.MINUTES);

        SecurityContextHolder.clearContext();
        return new CommonResponseDto(true, "Logged out successfully");
    }

}
