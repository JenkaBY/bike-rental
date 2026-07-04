package com.github.jenkaby.bikerental.rental;

import com.github.jenkaby.bikerental.shared.exception.BikeRentalException;

public class RentalSigningVersionMismatchException extends BikeRentalException {

    public static final String ERROR_CODE = "agreement.signing.rental_version_mismatch";

    private static final String MESSAGE_TEMPLATE = "Rental %d version mismatch. Expected: %d, actual: %d";

    public RentalSigningVersionMismatchException(Long rentalId, Long expectedVersion, Long actualVersion) {
        super(MESSAGE_TEMPLATE.formatted(rentalId, expectedVersion, actualVersion), ERROR_CODE,
                new Details(rentalId, expectedVersion, actualVersion));
    }

    public Details getDetails() {
        return getParams()
                .map(d -> (Details) d)
                .orElseThrow(() -> new IllegalArgumentException("Expected Details in exception parameters"));
    }

    public record Details(Long rentalId, Long expectedVersion, Long actualVersion) {
    }
}
