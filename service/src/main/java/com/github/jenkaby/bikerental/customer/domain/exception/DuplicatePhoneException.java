package com.github.jenkaby.bikerental.customer.domain.exception;

import com.github.jenkaby.bikerental.shared.exception.ResourceConflictException;

public class DuplicatePhoneException extends ResourceConflictException {

    public DuplicatePhoneException(String resourceName, String identifier) {
        super(resourceName, identifier);
    }
}
