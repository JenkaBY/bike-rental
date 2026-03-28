package com.github.jenkaby.bikerental.finance.domain.exception;

import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
import com.github.jenkaby.bikerental.shared.exception.BikeRentalException;

public class InsufficientBalanceException extends BikeRentalException {

    public static final String ERROR_CODE = "finance.insufficient_balance";

    private static final String MESSAGE_TEMPLATE =
            "Insufficient wallet balance. Available: %s, requested deduction: %s";

    public InsufficientBalanceException(Money available, Money requested) {
        super(MESSAGE_TEMPLATE.formatted(available.amount(), requested.amount()), ERROR_CODE);
    }
}
