package com.github.jenkaby.bikerental.users.domain.exception;

import com.github.jenkaby.bikerental.shared.exception.BikeRentalException;
import com.github.jenkaby.bikerental.shared.web.advice.ErrorCodes;

public class InvalidCurrentPasswordException extends BikeRentalException {

    public static final String ERROR_CODE = ErrorCodes.CURRENT_PASSWORD_INVALID;

    public InvalidCurrentPasswordException() {
        super("Current password is incorrect", ERROR_CODE);
    }
}
