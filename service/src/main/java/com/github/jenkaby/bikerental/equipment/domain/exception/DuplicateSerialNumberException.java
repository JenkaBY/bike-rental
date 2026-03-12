package com.github.jenkaby.bikerental.equipment.domain.exception;

import com.github.jenkaby.bikerental.shared.exception.ResourceConflictException;

public class DuplicateSerialNumberException extends ResourceConflictException {

    public static final String ERROR_CODE = "equipment.serial.duplicate";

    public DuplicateSerialNumberException(String resourceName, String identifier) {
        super(resourceName, identifier, ERROR_CODE);
    }

    public DuplicateSerialNumberException(Class<?> cls, String identifier) {
        super(cls.getSimpleName(), identifier, ERROR_CODE);
    }
}
