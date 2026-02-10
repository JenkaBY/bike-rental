package com.github.jenkaby.bikerental.rental.domain.exception;

import com.github.jenkaby.bikerental.shared.exception.BikeRentalException;
import lombok.Getter;

@Getter
public class PrepaymentRequiredException extends BikeRentalException {

    private static final String MESSAGE = "Prepayment must be received before starting rental";

    private final Long rentalId;

    public PrepaymentRequiredException(Long rentalId) {
        super(MESSAGE);
        this.rentalId = rentalId;
    }
}
