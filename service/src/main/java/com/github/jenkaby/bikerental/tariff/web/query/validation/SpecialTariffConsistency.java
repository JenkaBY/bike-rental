package com.github.jenkaby.bikerental.tariff.web.query.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;

@Documented
@Constraint(validatedBy = SpecialTariffConsistencyValidator.class)
@Target({TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface SpecialTariffConsistency {

    String message() default "error.validation.special_tariff_inconsistent";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}


