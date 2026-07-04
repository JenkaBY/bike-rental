package com.github.jenkaby.bikerental.agreement.domain.exception;

import com.github.jenkaby.bikerental.shared.exception.BikeRentalException;

public class AgreementAlreadySignedException extends BikeRentalException {

    public static final String ERROR_CODE = "agreement.signing.already_signed";

    private static final String MESSAGE_TEMPLATE = "Rental %d has already been signed";

    public AgreementAlreadySignedException(Long rentalId) {
        super(MESSAGE_TEMPLATE.formatted(rentalId), ERROR_CODE, new Details(rentalId));
    }

    public Details getDetails() {
        return getParams()
                .map(d -> (Details) d)
                .orElseThrow(() -> new IllegalArgumentException("Expected Details in exception parameters"));
    }

    public record Details(Long rentalId) {
    }
}
