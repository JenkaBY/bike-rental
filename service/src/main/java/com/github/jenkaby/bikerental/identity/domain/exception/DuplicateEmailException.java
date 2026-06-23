package com.github.jenkaby.bikerental.identity.domain.exception;

import com.github.jenkaby.bikerental.shared.exception.ResourceConflictException;
import com.github.jenkaby.bikerental.shared.web.advice.ErrorCodes;

public class DuplicateEmailException extends ResourceConflictException {

    public static final String ERROR_CODE = ErrorCodes.EMAIL_DUPLICATE;

    public DuplicateEmailException(String email) {
        super("User", email, ERROR_CODE);
    }
}
