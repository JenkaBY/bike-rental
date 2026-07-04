package com.github.jenkaby.bikerental.rental;

import com.github.jenkaby.bikerental.rental.domain.model.RentalStatus;
import com.github.jenkaby.bikerental.shared.exception.BikeRentalException;

public class RentalNotAwaitingSignatureException extends BikeRentalException {

    public static final String ERROR_CODE = "agreement.signing.rental_not_awaiting_signature";

    private static final String MESSAGE_TEMPLATE = "Rental %d is not awaiting signature. Current status: %s";

    public RentalNotAwaitingSignatureException(Long rentalId, RentalStatus currentStatus) {
        super(MESSAGE_TEMPLATE.formatted(rentalId, currentStatus), ERROR_CODE, new Details(rentalId, currentStatus));
    }

    public Details getDetails() {
        return getParams()
                .map(d -> (Details) d)
                .orElseThrow(() -> new IllegalArgumentException("Expected Details in exception parameters"));
    }

    public record Details(Long rentalId, RentalStatus currentStatus) {
    }
}
