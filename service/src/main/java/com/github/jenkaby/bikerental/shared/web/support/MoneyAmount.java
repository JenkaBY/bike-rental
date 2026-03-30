package com.github.jenkaby.bikerental.shared.web.support;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@NotNull
@Digits(integer = 17, fraction = 2)
@Target({TYPE_USE, METHOD, FIELD, ANNOTATION_TYPE, PARAMETER})
@Retention(RUNTIME)
@Constraint(validatedBy = {})
@Documented
public @interface MoneyAmount {

    String message() default "must be a valid monetary amount";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
