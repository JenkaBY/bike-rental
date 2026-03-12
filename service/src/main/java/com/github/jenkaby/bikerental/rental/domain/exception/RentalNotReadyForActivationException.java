package com.github.jenkaby.bikerental.rental.domain.exception;

import com.github.jenkaby.bikerental.shared.exception.BikeRentalException;
import lombok.Getter;

import java.util.List;

/**
 * Exception thrown when rental cannot be activated because required fields are missing.
 * <p>
 * Rental must have all required fields filled before activation:
 * - customerId
 * - equipmentId
 * - tariffId
 * - plannedDuration
 * - estimatedCost
 */
@Getter
public class RentalNotReadyForActivationException extends BikeRentalException {

    public static final String ERROR_CODE = "rental.activation.not_ready";

    private static final String MESSAGE_TEMPLATE = "Rental cannot be activated. Missing required fields: %s";

    private final List<String> missingFields;

    public RentalNotReadyForActivationException(List<String> missingFields) {
        super(MESSAGE_TEMPLATE.formatted(String.join(", ", missingFields)), ERROR_CODE);
        this.missingFields = missingFields;
    }

    public MissingFields getDetails() {
        return this.getParams().map(params -> (MissingFields) params)
                .orElseThrow(() -> new IllegalArgumentException("Expected MissingFields in exception parameters"));
    }

    public record MissingFields(List<String> fields) {
    }
}
