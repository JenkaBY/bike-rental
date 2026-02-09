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

    private static final String MESSAGE_TEMPLATE = "Rental cannot be activated. Missing required fields: %s";

    private final List<String> missingFields;

    public RentalNotReadyForActivationException(List<String> missingFields) {
        super(MESSAGE_TEMPLATE.formatted(String.join(", ", missingFields)));
        this.missingFields = missingFields;
    }
}
