package com.github.jenkaby.bikerental.shared.web.support;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Size(max = 50, message = "Slug must not exceed 50 characters")
@NotBlank
@Pattern(regexp = "^[A-Z0-9-_]+$", message = "Slug must contain only uppercase letters, digits, hyphens and underscore")
@Target({TYPE_USE, METHOD, FIELD, ANNOTATION_TYPE, PARAMETER})
@Retention(RUNTIME)
@Constraint(validatedBy = {})
@Documented
public @interface Slug {

    @AliasFor(annotation = NotBlank.class)
    String message() default "slug is required";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
