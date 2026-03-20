package com.github.jenkaby.bikerental.tariff.web.command.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({TYPE, PARAMETER})
@Retention(RUNTIME)
@Constraint(validatedBy = {TariffV2PricingConstraintValidator.class})
@Documented
public @interface ValidTariffV2Pricing {

    String message() default "tariff.validation.invalid_pricing";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

