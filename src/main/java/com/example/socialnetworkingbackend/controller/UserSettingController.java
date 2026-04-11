package com.example.socialnetworkingbackend.controller;

import com.example.socialnetworkingbackend.base.RestApiV1;
import com.example.socialnetworkingbackend.base.VsResponseUtil;
import com.example.socialnetworkingbackend.constant.UrlConstant;
import com.example.socialnetworkingbackend.domain.dto.request.UserSettingRequestDto;
import com.example.socialnetworkingbackend.security.CurrentUser;
import com.example.socialnetworkingbackend.security.UserPrincipal;
import com.example.socialnetworkingbackend.service.UserSettingService;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import jakarta.validation.Valid;

@RestApiV1
@RequiredArgsConstructor
@Validated
public class UserSettingController {

    private final UserSettingService userSettingService;

    @PutMapping(UrlConstant.UserSetting.UPDATE_SETTING)
    public ResponseEntity<?> updateUserSetting(
            @Valid @RequestBody UserSettingRequestDto request,
            @Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal) {

        userSettingService.updateUserSetting(userPrincipal, request);
        return VsResponseUtil.success("User setting updated successfully");
    }
}