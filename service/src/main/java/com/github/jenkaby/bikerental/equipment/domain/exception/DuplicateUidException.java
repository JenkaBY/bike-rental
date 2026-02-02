package com.github.jenkaby.bikerental.equipment.domain.exception;

import com.github.jenkaby.bikerental.shared.exception.ResourceConflictException;

public class DuplicateUidException extends ResourceConflictException {
    public DuplicateUidException(String resourceName, String identifier) {
        super(resourceName, identifier);
    }

    public DuplicateUidException(Class<?> cls, String identifier) {
        super(cls, identifier);
    }
}
