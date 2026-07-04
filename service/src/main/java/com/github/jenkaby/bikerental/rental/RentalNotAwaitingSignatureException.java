package com.github.jenkaby.bikerental.rental;

import com.github.jenkaby.bikerental.rental.domain.model.RentalStatus;
import lombok.Getter;

@Getter
public class RentalNotAwaitingSignatureException extends RuntimeException {

    private final Long rentalId;
    private final RentalStatus currentStatus;

    public RentalNotAwaitingSignatureException(Long rentalId, RentalStatus currentStatus) {
        super("Rental %d is not awaiting signature. Current status: %s".formatted(rentalId, currentStatus));
        this.rentalId = rentalId;
        this.currentStatus = currentStatus;
    }
}
