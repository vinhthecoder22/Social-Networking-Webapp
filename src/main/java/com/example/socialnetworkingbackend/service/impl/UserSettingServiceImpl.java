package com.example.socialnetworkingbackend.service.impl;

import com.example.socialnetworkingbackend.constant.ErrorMessage;
import com.example.socialnetworkingbackend.constant.LanguageSetting;
import com.example.socialnetworkingbackend.constant.ThemeSetting;
import com.example.socialnetworkingbackend.domain.dto.request.UserSettingRequestDto;
import com.example.socialnetworkingbackend.domain.entity.User;
import com.example.socialnetworkingbackend.domain.entity.UserSetting;
import com.example.socialnetworkingbackend.exception.NotFoundException;
import com.example.socialnetworkingbackend.repository.UserRepository;
import com.example.socialnetworkingbackend.repository.UserSettingRepository;
import com.example.socialnetworkingbackend.security.UserPrincipal;
import com.example.socialnetworkingbackend.service.UserSettingService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserSettingServiceImpl implements UserSettingService {

    private final UserRepository userRepository;
    private final UserSettingRepository userSettingRepository;

    @Override
    @Transactional
    public void updateUserSetting(UserPrincipal principal, UserSettingRequestDto request) {

        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new NotFoundException(
                        ErrorMessage.User.ERR_NOT_FOUND_ID,
                        new String[]{principal.getId()}
                ));

        UserSetting setting = userSettingRepository.findByUser(user)
                .orElseGet(() -> createDefaultSetting(user));

        applyUpdate(setting, request);

        userSettingRepository.save(setting);
    }

    private UserSetting createDefaultSetting(User user) {
        UserSetting setting = new UserSetting();
        setting.setUser(user);

        setting.setTheme(ThemeSetting.LIGHT);
        setting.setLanguage(LanguageSetting.EN);

        return setting;
    }

    private void applyUpdate(UserSetting setting, UserSettingRequestDto request) {
        setting.setTheme(request.getTheme());
        setting.setLanguage(request.getLanguage());
    }
}