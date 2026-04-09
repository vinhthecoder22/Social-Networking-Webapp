package com.example.socialnetworkingbackend.validation.annotation;

import com.example.socialnetworkingbackend.validation.common.PasswordValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = PasswordValidator.class)
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPassword {

    String message() default "invalid.password";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}