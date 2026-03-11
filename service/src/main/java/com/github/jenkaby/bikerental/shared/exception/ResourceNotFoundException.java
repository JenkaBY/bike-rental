package com.github.jenkaby.bikerental.shared.exception;

import lombok.Getter;
import org.jspecify.annotations.NonNull;

@Getter
public class ResourceNotFoundException extends BikeRentalException {

    public static final String ERROR_CODE = "shared.resource.not_found";

    private static final String MESSAGE_TEMPLATE = "%s with identifier '%s' not found";

    private final String resourceName;
    private final String identifier;

    public ResourceNotFoundException(String resourceName, String identifier) {
        super(MESSAGE_TEMPLATE.formatted(resourceName, identifier), ERROR_CODE);
        this.resourceName = resourceName;
        this.identifier = identifier;
    }

    public ResourceNotFoundException(@NonNull Class<?> cls, @NonNull Object identifier) {
        this(cls.getSimpleName(), identifier.toString());
    }
}