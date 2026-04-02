package com.github.jenkaby.bikerental.finance.domain.exception;

import com.github.jenkaby.bikerental.shared.exception.BikeRentalException;
import com.github.jenkaby.bikerental.shared.web.advice.ErrorCodes;
import lombok.Getter;

@Getter
public class InsufficientHoldException extends BikeRentalException {

    public static final String ERROR_CODE = ErrorCodes.INSUFFICIENT_HOLD;

    private static final String MESSAGE_TEMPLATE = "No hold transaction found for rental %d";

    public InsufficientHoldException(Long rentalId) {
        super(
                MESSAGE_TEMPLATE.formatted(rentalId),
                ERROR_CODE,
                new Details(rentalId)
        );
    }

    public Details getDetails() {
        return getParams().map(params -> (Details) params)
                .orElseThrow(() -> new IllegalArgumentException("Expected Details in exception parameters"));
    }

    public record Details(Long rentalId) {
    }
}
