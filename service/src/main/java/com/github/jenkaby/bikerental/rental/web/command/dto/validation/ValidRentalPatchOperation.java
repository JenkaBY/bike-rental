package com.github.jenkaby.bikerental.rental.web.command.dto.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;


@Documented
@Constraint(validatedBy = RentalPatchOperationValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidRentalPatchOperation {

    String message() default "Invalid patch operation";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
