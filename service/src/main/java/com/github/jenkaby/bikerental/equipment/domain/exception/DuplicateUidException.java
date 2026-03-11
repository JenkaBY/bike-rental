package com.github.jenkaby.bikerental.equipment.domain.exception;

import com.github.jenkaby.bikerental.shared.exception.ResourceConflictException;

public class DuplicateUidException extends ResourceConflictException {

    public static final String ERROR_CODE = "equipment.uid.duplicate";

    public DuplicateUidException(String resourceName, String identifier) {
        super(resourceName, identifier, ERROR_CODE);
    }

    public DuplicateUidException(Class<?> cls, String identifier) {
        super(cls.getSimpleName(), identifier, ERROR_CODE);
    }
}
