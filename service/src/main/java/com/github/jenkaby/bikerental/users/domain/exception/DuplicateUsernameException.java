package com.github.jenkaby.bikerental.users.domain.exception;

import com.github.jenkaby.bikerental.shared.exception.ResourceConflictException;
import com.github.jenkaby.bikerental.shared.web.advice.ErrorCodes;

public class DuplicateUsernameException extends ResourceConflictException {

    public static final String ERROR_CODE = ErrorCodes.USERNAME_DUPLICATE;

    public DuplicateUsernameException(String username) {
        super("User", username, ERROR_CODE);
    }
}
