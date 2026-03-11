package com.github.jenkaby.bikerental.rental.domain.exception;

import com.github.jenkaby.bikerental.shared.exception.BikeRentalException;

/**
 * Exception thrown when rental update request contains invalid data or violates business rules.
 */
public class InvalidRentalUpdateException extends BikeRentalException {

    public static final String ERROR_CODE = "rental.update.invalid";

    public InvalidRentalUpdateException(String message) {
        super(message, ERROR_CODE);
    }
}
