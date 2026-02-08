package com.github.jenkaby.bikerental.rental.web.command.dto.validation;

import com.github.jenkaby.bikerental.rental.domain.model.RentalStatus;
import com.github.jenkaby.bikerental.rental.web.command.dto.RentalPatchOperation;
import com.github.jenkaby.bikerental.rental.web.command.dto.RentalUpdateJsonPatchRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Validates the entire JSON Patch request for Rental.
 * Checks cross-operation constraints like duration/startTime pairing.
 */
public class RentalPatchRequestValidator implements ConstraintValidator<ValidRentalPatchRequest, RentalUpdateJsonPatchRequest> {

    private static final Set<String> VALID_STATUSES = Arrays.stream(RentalStatus.values())
            .map(Enum::name)
            .collect(Collectors.toSet());


    @Override
    public boolean isValid(RentalUpdateJsonPatchRequest request, ConstraintValidatorContext context) {
        if (request == null || request.getOperations() == null) {
            return true;
        }

        boolean isValid = true;
        context.disableDefaultConstraintViolation();

        List<RentalPatchOperation> operations = request.getOperations();

        // Check if duration and startTime are provided together
        boolean hasDuration = operations.stream()
                .anyMatch(op -> "/duration".equals(op.getPath()) && op.getValue() != null);
        boolean hasStartTime = operations.stream()
                .anyMatch(op -> "/startTime".equals(op.getPath()) && op.getValue() != null);

        if (hasDuration && !hasStartTime) {
            context.buildConstraintViolationWithTemplate(
                            "Both duration and startTime must be provided together")
                    .addConstraintViolation();
            isValid = false;
        }

        if (hasStartTime && !hasDuration) {
            context.buildConstraintViolationWithTemplate(
                            "Both duration and startTime must be provided together")
                    .addConstraintViolation();
            isValid = false;
        }

        // Validate status values using RentalStatus enum
        boolean hasInvalidStatus = operations.stream()
                .filter(op -> "/status".equals(op.getPath()) && op.getValue() != null)
                .anyMatch(op -> {
                    String statusValue = op.getValue().toString();
                    try {
                        RentalStatus.valueOf(statusValue);
                        return false; // Valid status
                    } catch (IllegalArgumentException e) {
                        context.buildConstraintViolationWithTemplate(
                                        String.format("Invalid status value '%s'. Allowed values: %s",
                                                statusValue, VALID_STATUSES))
                                .addConstraintViolation();
                        return true; // Invalid status
                    }
                });

        if (hasInvalidStatus) {
            isValid = false;
        }

        return isValid;
    }
}
