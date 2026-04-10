package com.example.socialnetworkingbackend.validation.annotation;

import com.example.socialnetworkingbackend.validation.common.FileImageValidator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.PARAMETER})
@Constraint(validatedBy = {FileImageValidator.class})
public @interface ValidFileImage {

  String message() default "invalid.file-image";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

}
