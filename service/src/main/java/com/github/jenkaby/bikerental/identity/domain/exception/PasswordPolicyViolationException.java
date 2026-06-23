package com.github.jenkaby.bikerental.identity.domain.exception;

import com.github.jenkaby.bikerental.shared.exception.BikeRentalException;
import com.github.jenkaby.bikerental.shared.web.advice.ErrorCodes;

public class PasswordPolicyViolationException extends BikeRentalException {

    public static final String ERROR_CODE = ErrorCodes.PASSWORD_POLICY_VIOLATION;

    public PasswordPolicyViolationException(String message) {
        super(message, ERROR_CODE);
    }
}
