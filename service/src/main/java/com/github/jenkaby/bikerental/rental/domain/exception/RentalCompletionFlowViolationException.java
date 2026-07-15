package com.github.jenkaby.bikerental.rental.domain.exception;

import com.github.jenkaby.bikerental.shared.exception.BikeRentalException;
import lombok.Getter;

@Getter
public class RentalCompletionFlowViolationException extends BikeRentalException {

    public static final String ERROR_CODE = "rental.completion.flow_violation";

    private static final String MESSAGE_TEMPLATE = "Rental %d cannot be completed directly via POST /api/rentals/return; " +
            "return the last equipment via the quote-based confirmation flow (POST /api/rentals/{rentalId}/returns)";

    public RentalCompletionFlowViolationException(Long rentalId) {
        super(MESSAGE_TEMPLATE.formatted(rentalId), ERROR_CODE, new Details(rentalId));
    }

    public Details getDetails() {
        return getParams().map(Details.class::cast)
                .orElseThrow(() -> new IllegalArgumentException("Expected Details in exception parameters"));
    }

    public record Details(Long rentalId) {
    }
}
