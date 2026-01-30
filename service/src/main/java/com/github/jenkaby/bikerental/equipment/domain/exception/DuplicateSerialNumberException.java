package com.github.jenkaby.bikerental.equipment.domain.exception;

import com.github.jenkaby.bikerental.shared.exception.ResourceConflictException;

public class DuplicateSerialNumberException extends ResourceConflictException {
    public DuplicateSerialNumberException(String resourceName, String identifier) {
        super(resourceName, identifier);
    }
}
