package com.example.socialnetworkingbackend.validation.common;

import com.example.socialnetworkingbackend.config.PasswordProperties;
import com.example.socialnetworkingbackend.validation.annotation.ValidPassword;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PasswordValidator implements ConstraintValidator<ValidPassword, String> {

    private final PasswordProperties props;

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {

        if (password == null || password.trim().isEmpty()) {
            buildMessage(context, "Password must not be blank");
            return false;
        }

        if (password.length() < props.getMinLength()) {
            buildMessage(context, "Password must be at least " + props.getMinLength() + " characters");
            return false;
        }

        if (props.isRequireLowercase() && !password.matches(".*[a-z].*")) {
            buildMessage(context, "Password must contain at least one lowercase letter");
            return false;
        }

        if (props.isRequireNumber() && !password.matches(".*\\d.*")) {
            buildMessage(context, "Password must contain at least one number");
            return false;
        }

        if (props.isRequireSpecial()) {
            String specialChars = props.getSpecialChars();
            boolean hasSpecial = false;
            for (char c : password.toCharArray()) {
                if (specialChars.indexOf(c) >= 0) {
                    hasSpecial = true;
                    break;
                }
            }
            if (!hasSpecial) {
                buildMessage(context, "Password must contain at least one special character");
                return false;
            }
        }

        return true;
    }

    private void buildMessage(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message)
                .addConstraintViolation();
    }
}