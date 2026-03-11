package com.github.jenkaby.bikerental.customer.domain.exception;

import com.github.jenkaby.bikerental.shared.exception.ResourceConflictException;

public class DuplicatePhoneException extends ResourceConflictException {

    public static final String ERROR_CODE = "customer.phone.duplicate";

    public DuplicatePhoneException(String resourceName, String identifier) {
        super(resourceName, identifier, ERROR_CODE);
    }
}
