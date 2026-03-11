package com.github.jenkaby.bikerental.shared.exception;

/**
 * Base exception for bike rental application.
 */
public class BikeRentalException extends RuntimeException {

    private final String errorCode;

    public BikeRentalException() {
        this.errorCode = null;
    }

    public BikeRentalException(String message) {
        super(message);
        this.errorCode = null;
    }

    protected BikeRentalException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public BikeRentalException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = null;
    }

    public BikeRentalException(Throwable cause) {
        super(cause);
        this.errorCode = null;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
