package com.github.jenkaby.bikerental.shared.exception;

import lombok.Getter;

@Getter
public class ResourceConflictException extends BikeRentalException {

    private static final String MESSAGE_TEMPLATE = "%s with identifier '%s' already exists";

    private final String resourceName;
    private final String identifier;

    public ResourceConflictException(String resourceName, String identifier) {
        super(MESSAGE_TEMPLATE.formatted(resourceName, identifier));
        this.resourceName = resourceName;
        this.identifier = identifier;
    }

    public ResourceConflictException(Class<?> cls, String identifier) {
        this(cls.getSimpleName(), identifier);
    }
}
