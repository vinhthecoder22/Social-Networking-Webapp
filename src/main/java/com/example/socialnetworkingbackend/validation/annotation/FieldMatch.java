package com.example.socialnetworkingbackend.validation.annotation;

import com.example.socialnetworkingbackend.validation.common.FieldMatchValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = FieldMatchValidator.class)
@Target({ ElementType.TYPE }) //
@Retention(RetentionPolicy.RUNTIME)
public @interface FieldMatch {

    String message() default "Fields do not match";

    String first();
    String second();

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
