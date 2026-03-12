package com.github.jenkaby.bikerental.shared.exception;

import org.jspecify.annotations.Nullable;

import java.util.Optional;

/**
 * Base exception for bike rental application.
 */
public class BikeRentalException extends RuntimeException {

    private final String errorCode;
    private final Object params;

    // TODO Revise constructor
    protected BikeRentalException(String message, String errorCode) {
        this(message, errorCode, null);
    }

    // TODO Revise constructor
    protected BikeRentalException(String message, String errorCode, @Nullable Object params) {
        super(message);
        this.errorCode = errorCode;
        this.params = params;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public Optional<Object> getParams() {
        return Optional.ofNullable(params);
    }
}
