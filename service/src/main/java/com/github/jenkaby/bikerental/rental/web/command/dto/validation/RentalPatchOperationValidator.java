package com.github.jenkaby.bikerental.rental.web.command.dto.validation;

import com.github.jenkaby.bikerental.rental.web.command.dto.RentalPatchOperation;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.Duration;
import java.util.Set;


public class RentalPatchOperationValidator implements ConstraintValidator<ValidRentalPatchOperation, RentalPatchOperation> {

    private static final Set<String> ALLOWED_PATHS = Set.of(
            "/customerId",
            "/equipmentIds",
            "/tariffId",
            "/duration",
            "/status"
    );

    @Override
    public boolean isValid(RentalPatchOperation operation, ConstraintValidatorContext context) {
        if (operation == null) {
            return true;
        }

        boolean isValid = true;
        context.disableDefaultConstraintViolation();

        // Validate operation type
        if (operation.getOp() == null) {
            context.buildConstraintViolationWithTemplate("Operation 'op' is required")
                    .addPropertyNode("op")
                    .addConstraintViolation();
            isValid = false;
        }

        // Validate path
        if (operation.getPath() == null || operation.getPath().isBlank()) {
            context.buildConstraintViolationWithTemplate("Path is required")
                    .addPropertyNode("path")
                    .addConstraintViolation();
            isValid = false;
        } else if (!operation.getPath().startsWith("/")) {
            context.buildConstraintViolationWithTemplate("Path must start with '/'")
                    .addPropertyNode("path")
                    .addConstraintViolation();
            isValid = false;
        } else if (!ALLOWED_PATHS.contains(operation.getPath())) {
            context.buildConstraintViolationWithTemplate(
                            String.format("Path '%s' is not allowed. Allowed paths: %s",
                                    operation.getPath(), ALLOWED_PATHS))
                    .addPropertyNode("path")
                    .addConstraintViolation();
            isValid = false;
        }

        // Validate value is provided (all supported operations require a value)
        if (operation.getOp() != null && operation.getValue() == null) {
            context.buildConstraintViolationWithTemplate(
                            String.format("Value is required for operation '%s'", operation.getOp().getValue()))
                    .addPropertyNode("value")
                    .addConstraintViolation();
            isValid = false;
        }
        if (operation.getOp() != null && "/duration".equals(operation.getPath()) && operation.getValue() != null) {
            // For duration, value must be a valid ISO-8601 duration string
            try {
                Duration.parse(operation.getValue().toString());
            } catch (Exception e) {
                context.buildConstraintViolationWithTemplate(
                                "Value for path '/duration' must be a valid ISO-8601 duration string, e.g. 'PT1H30M'")
                        .addPropertyNode("value")
                        .addConstraintViolation();
                isValid = false;
            }
        }
//        if (operation.getOp() != null && "/equipmentIds".equals(operation.getPath()) && operation.getValue() != null) {
//            // For equipmentIds, value must be an array
//            try {
//                Duration.parse(operation.getValue().toString());
//            } catch (Exception e) {
//                context.buildConstraintViolationWithTemplate(
//                                "Value for path '/equipmentIds' must be an array of int64")
//                        .addPropertyNode("value")
//                        .addConstraintViolation();
//                isValid = false;
//            }
//        }
        return isValid;
    }
}
