package com.github.jenkaby.bikerental.shared.exception;

import lombok.Getter;
import org.jspecify.annotations.NonNull;

import static com.github.jenkaby.bikerental.shared.web.advice.ErrorCodes.REFERENCE_NOT_FOUND;

@Getter
public class ReferenceNotFoundException extends BikeRentalException {

    public static final String ERROR_CODE = REFERENCE_NOT_FOUND;

    private static final String MESSAGE_TEMPLATE = "Referenced %s with identifier '%s' not found";

    public ReferenceNotFoundException(String resourceName, String identifier) {
        super(MESSAGE_TEMPLATE.formatted(resourceName, identifier), ERROR_CODE, new ResourceDetails(resourceName, identifier));
    }

    public ReferenceNotFoundException(@NonNull Class<?> cls, @NonNull Object identifier) {
        this(cls.getSimpleName(), identifier.toString());
    }

    public ResourceDetails getDetails() {
        return getParams().map(params -> (ResourceDetails) params)
                .orElseThrow(() -> new IllegalArgumentException("Expected ResourceDetails in exception parameters"));
    }

    public record ResourceDetails(String resourceName, String identifier) {
    }
}