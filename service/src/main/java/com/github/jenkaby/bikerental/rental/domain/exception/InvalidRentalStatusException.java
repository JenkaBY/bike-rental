package com.github.jenkaby.bikerental.rental.domain.exception;

import com.github.jenkaby.bikerental.rental.domain.model.RentalStatus;
import com.github.jenkaby.bikerental.shared.exception.BikeRentalException;
import lombok.Getter;

@Getter
public class InvalidRentalStatusException extends BikeRentalException {

    public static final String ERROR_CODE = "rental.status.invalid";

    private static final String MESSAGE_TEMPLATE = "Cannot perform operation on rental with status %s. Expected status: %s";

    private final RentalStatus currentStatus;
    private final RentalStatus expectedStatus;

    public InvalidRentalStatusException(RentalStatus currentStatus, RentalStatus expectedStatus) {
        super(MESSAGE_TEMPLATE.formatted(currentStatus, expectedStatus), ERROR_CODE);
        this.currentStatus = currentStatus;
        this.expectedStatus = expectedStatus;
    }
}
