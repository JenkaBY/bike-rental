package com.github.jenkaby.bikerental.rental.web.command.dto.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = RentalPatchRequestValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidRentalPatchRequest {

    String message() default "Invalid patch request";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
