package com.github.jenkaby.bikerental.shared.exception;

import lombok.Getter;
import org.jspecify.annotations.NonNull;

@Getter
public class ReferenceNotFoundException extends BikeRentalException {

    public static final String ERROR_CODE = "shared.reference.not_found";

    private static final String MESSAGE_TEMPLATE = "Referenced %s with identifier '%s' not found";

    private final String resourceName;
    private final String identifier;

    public ReferenceNotFoundException(String resourceName, String identifier) {
        super(MESSAGE_TEMPLATE.formatted(resourceName, identifier), ERROR_CODE);
        this.resourceName = resourceName;
        this.identifier = identifier;
    }

    public ReferenceNotFoundException(@NonNull Class<?> cls, @NonNull Object identifier) {
        this(cls.getSimpleName(), identifier.toString());
    }
}