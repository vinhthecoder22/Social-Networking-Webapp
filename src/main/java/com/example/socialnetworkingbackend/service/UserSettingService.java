package com.example.socialnetworkingbackend.service;

import com.example.socialnetworkingbackend.domain.dto.request.UserSettingRequestDto;
import com.example.socialnetworkingbackend.security.UserPrincipal;

public interface UserSettingService {

    void updateUserSetting(UserPrincipal principal, UserSettingRequestDto request);
}