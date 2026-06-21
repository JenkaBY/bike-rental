package com.github.jenkaby.bikerental.shared.exception;

import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
import com.github.jenkaby.bikerental.shared.web.advice.ErrorCodes;

import java.math.BigDecimal;

public class InsufficientBalanceException extends BikeRentalException {

    public static final String ERROR_CODE = ErrorCodes.INSUFFICIENT_BALANCE;

    private static final String MESSAGE_TEMPLATE =
            "Insufficient wallet balance. Available: %s, requested deduction: %s";

    public InsufficientBalanceException(Money available, Money requested) {
        super(MESSAGE_TEMPLATE.formatted(available.amount(), requested.amount()), ERROR_CODE,
                new Details(available.amount(), requested.amount()));
    }

    public Details getDetails() {
        return getParams().map(p -> (Details) p)
                .orElseThrow(() -> new IllegalArgumentException("Expected Details in exception parameters"));
    }

    public record Details(BigDecimal available, BigDecimal requested) {}
}
