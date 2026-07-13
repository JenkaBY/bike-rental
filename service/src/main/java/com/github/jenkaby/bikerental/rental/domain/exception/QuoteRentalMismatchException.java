package com.github.jenkaby.bikerental.rental.domain.exception;

import com.github.jenkaby.bikerental.shared.exception.BikeRentalException;
import lombok.Getter;

import java.util.UUID;


@Getter
public class QuoteRentalMismatchException extends BikeRentalException {

    public static final String ERROR_CODE = "rental.quote.mismatch";

    private static final String MESSAGE_TEMPLATE = "Cost quote '%s' is inconsistent with rental %d: %s";

    public QuoteRentalMismatchException(UUID quoteId, Long rentalId, String reason) {
        super(MESSAGE_TEMPLATE.formatted(quoteId, rentalId, reason), ERROR_CODE, new Details(quoteId, rentalId, reason));
    }

    public Details getDetails() {
        return getParams().map(Details.class::cast)
                .orElseThrow(() -> new IllegalArgumentException("Expected Details in exception parameters"));
    }

    public record Details(UUID quoteId, Long rentalId, String reason) {
    }
}
