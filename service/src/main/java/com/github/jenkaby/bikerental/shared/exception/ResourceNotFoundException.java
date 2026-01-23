package com.github.jenkaby.bikerental.shared.exception;

import lombok.Getter;

@Getter
public class ResourceNotFoundException extends BikeRentalException {

    private static final String MESSAGE_TEMPLATE = "%s with identifier '%s' not found";

    private final String resourceName;
    private final String identifier;

    public ResourceNotFoundException(String resourceName, String identifier) {

        super(MESSAGE_TEMPLATE.formatted(resourceName, identifier));
        this.resourceName = resourceName;
        this.identifier = identifier;
    }
}