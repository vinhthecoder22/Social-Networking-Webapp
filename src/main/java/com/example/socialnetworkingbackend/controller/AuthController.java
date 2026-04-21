package com.example.socialnetworkingbackend.controller;

import com.example.socialnetworkingbackend.base.RestApiV1;
import com.example.socialnetworkingbackend.base.VsResponseUtil;
import com.example.socialnetworkingbackend.constant.UrlConstant;
import com.example.socialnetworkingbackend.domain.dto.request.LoginRequestDto;
import com.example.socialnetworkingbackend.domain.dto.request.RegisterRequestDto;
import com.example.socialnetworkingbackend.domain.dto.request.TokenRefreshRequestDto;
import com.example.socialnetworkingbackend.domain.dto.response.RegisterResponseDto;
import com.example.socialnetworkingbackend.security.CurrentUser;
import com.example.socialnetworkingbackend.security.UserPrincipal;
import com.example.socialnetworkingbackend.service.AuthService;
import com.example.socialnetworkingbackend.service.UserService;
import com.example.socialnetworkingbackend.service.impl.FirebaseAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestHeader;

@Log4j2
@RequiredArgsConstructor
@Validated
@RestApiV1
@Tag(name = "Login", description = "Các API đăng nhập với Tài khoản mật khẩu và Google, Facebook")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private final FirebaseAuthService firebaseAuthService;

    @Operation(summary = "API Đăng ký tài khoản")
    @PostMapping(UrlConstant.Auth.REGISTER)
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequestDto req) {
        RegisterResponseDto response = authService.register(req);
        return VsResponseUtil.success(HttpStatus.CREATED, response);
    }

    @Operation(summary = "Đăng nhập bằng username & password", description = "Truyền vào username và password hợp lệ để nhận access token & refresh token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Đăng nhập thành công"),
            @ApiResponse(responseCode = "401", description = "Sai thông tin đăng nhập")
    })
    @PostMapping(UrlConstant.Auth.LOGIN)
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDto request, HttpServletRequest requestHttp) {
        return VsResponseUtil.success(authService.login(request, requestHttp));
    }

    @Operation(summary = "API Refresh Token")
    @PostMapping(UrlConstant.Auth.REFRESH_TOKEN)
    public ResponseEntity<?> refreshToken(@Valid @RequestBody TokenRefreshRequestDto request) {
        return VsResponseUtil.success(authService.refresh(request));
    }

    @Operation(summary = "API Lấy thông tin người dùng hiện tại")
    @GetMapping(UrlConstant.Auth.ME)
    public ResponseEntity<?> getCurrentUser(
            @Parameter(hidden = true) @CurrentUser UserPrincipal principal) {
        if (principal == null) {
            return VsResponseUtil.error(HttpStatus.UNAUTHORIZED, HttpStatus.UNAUTHORIZED.getReasonPhrase());
        }
        return VsResponseUtil.success(userService.getCurrentUser(principal));
    }

    @Operation(summary = "API Logout", description = "Blacklist access token & refresh token, xóa cookie")
    @PostMapping(UrlConstant.Auth.LOGOUT)
    public ResponseEntity<?> logout(HttpServletRequest request,
                                    HttpServletResponse response,
                                    @RequestHeader(value = "X-Refresh-Token", required = false) String refreshToken) {

        Cookie clearAccessToken = new Cookie("accessToken", "");
        clearAccessToken.setMaxAge(0);
        clearAccessToken.setPath("/");
        clearAccessToken.setHttpOnly(true);
        clearAccessToken.setSecure(true);
        response.addCookie(clearAccessToken);

        Cookie clearRefreshToken = new Cookie("refreshToken", "");
        clearRefreshToken.setMaxAge(0);
        clearRefreshToken.setPath("/");
        clearRefreshToken.setHttpOnly(true);
        clearRefreshToken.setSecure(true);
        response.addCookie(clearRefreshToken);
        return VsResponseUtil.success(authService.logout(request, refreshToken));
    }

    @Operation(summary = "API Đăng nhập bằng Firebase (Google)", description = "Gửi ID Token từ Firebase Client để nhận Access Token")
    @PostMapping("/firebase-login")
    public ResponseEntity<?> firebaseLogin(
            @Valid @RequestBody com.example.socialnetworkingbackend.domain.dto.request.FirebaseLoginRequest request,
            HttpServletRequest httpRequest) {
        return VsResponseUtil.success(firebaseAuthService.loginWithFirebase(request, httpRequest));
    }
}
