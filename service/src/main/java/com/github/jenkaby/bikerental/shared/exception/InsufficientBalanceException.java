package com.github.jenkaby.bikerental.shared.exception;

import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
import com.github.jenkaby.bikerental.shared.web.advice.ErrorCodes;

public class InsufficientBalanceException extends BikeRentalException {

    public static final String ERROR_CODE = ErrorCodes.INSUFFICIENT_BALANCE;

    private static final String MESSAGE_TEMPLATE =
            "Insufficient wallet balance. Available: %s, requested deduction: %s";

    public InsufficientBalanceException(Money available, Money requested) {
        super(MESSAGE_TEMPLATE.formatted(available.amount(), requested.amount()), ERROR_CODE);
    }
}
