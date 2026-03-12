package com.github.jenkaby.bikerental.shared.exception;

import lombok.Getter;

import static com.github.jenkaby.bikerental.shared.web.advice.ErrorCodes.RESOURCE_CONFLICT;

@Getter
public class ResourceConflictException extends BikeRentalException {

    public static final String ERROR_CODE = RESOURCE_CONFLICT;

    private static final String MESSAGE_TEMPLATE = "%s with identifier '%s' already exists";

    public ResourceConflictException(String resourceName, String identifier) {
        this(resourceName, identifier, ERROR_CODE);
    }

    protected ResourceConflictException(String resourceName, String identifier, String errorCode) {
        super(MESSAGE_TEMPLATE.formatted(resourceName, identifier), errorCode, new ResourceDetails(resourceName, identifier));
    }

    public ResourceConflictException(Class<?> cls, String identifier) {
        this(cls.getSimpleName(), identifier);
    }

    public ResourceDetails getDetails() {
        return getParams().map(params -> (ResourceConflictException.ResourceDetails) params)
                .orElseThrow(() -> new IllegalArgumentException("Expected ResourceDetails in exception parameters"));
    }

    public record ResourceDetails(String resourceName, String identifier) {
    }
}
