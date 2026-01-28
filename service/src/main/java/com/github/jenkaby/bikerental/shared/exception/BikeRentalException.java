package com.github.jenkaby.bikerental.shared.exception;

/**
 * Base exception for bike rental application.
 */
public class BikeRentalException extends RuntimeException {

    public BikeRentalException() {
    }

    public BikeRentalException(String message) {
        super(message);
    }

    public BikeRentalException(String message, Throwable cause) {
        super(message, cause);
    }

    public BikeRentalException(Throwable cause) {
        super(cause);
    }
}
