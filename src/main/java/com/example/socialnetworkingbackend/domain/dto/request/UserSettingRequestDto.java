package com.example.socialnetworkingbackend.domain.dto.request;

import com.example.socialnetworkingbackend.constant.LanguageSetting;
import com.example.socialnetworkingbackend.constant.ThemeSetting;
import lombok.Data;
import jakarta.validation.constraints.NotNull;

@Data
public class UserSettingRequestDto {
    @NotNull
    private ThemeSetting theme;

    @NotNull
    private LanguageSetting language;
}
