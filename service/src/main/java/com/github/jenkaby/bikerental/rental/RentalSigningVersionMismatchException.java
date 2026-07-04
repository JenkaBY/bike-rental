package com.github.jenkaby.bikerental.rental;

import lombok.Getter;

@Getter
public class RentalSigningVersionMismatchException extends RuntimeException {

    private final Long rentalId;
    private final Long expectedVersion;
    private final Long actualVersion;

    public RentalSigningVersionMismatchException(Long rentalId, Long expectedVersion, Long actualVersion) {
        super("Rental %d version mismatch. Expected: %d, actual: %d".formatted(rentalId, expectedVersion, actualVersion));
        this.rentalId = rentalId;
        this.expectedVersion = expectedVersion;
        this.actualVersion = actualVersion;
    }
}
