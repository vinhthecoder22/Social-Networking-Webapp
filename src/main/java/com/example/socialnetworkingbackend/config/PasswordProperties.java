package com.example.socialnetworkingbackend.config;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Component
@Validated
@ConfigurationProperties(prefix = "app.security.password")
public class PasswordProperties {

    @Min(8)
    private int minLength;

    private boolean requireLowercase;
    private boolean requireNumber;
    private boolean requireSpecial;

    private String specialChars;
}