package com.example.socialnetworkingbackend.service;

import com.example.socialnetworkingbackend.domain.dto.request.LoginRequestDto;
import com.example.socialnetworkingbackend.domain.dto.request.RegisterRequestDto;
import com.example.socialnetworkingbackend.domain.dto.request.TokenRefreshRequestDto;
import com.example.socialnetworkingbackend.domain.dto.response.CommonResponseDto;
import com.example.socialnetworkingbackend.domain.dto.response.LoginResponseDto;
import com.example.socialnetworkingbackend.domain.dto.response.RegisterResponseDto;
import com.example.socialnetworkingbackend.domain.dto.response.TokenRefreshResponseDto;

import jakarta.servlet.http.HttpServletRequest;

public interface AuthService {
    RegisterResponseDto register(RegisterRequestDto request);

    LoginResponseDto login(LoginRequestDto request, HttpServletRequest httpServletRequest);

    TokenRefreshResponseDto refresh(TokenRefreshRequestDto request);

    CommonResponseDto logout(HttpServletRequest request);

}
