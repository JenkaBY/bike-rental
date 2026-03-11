package com.github.jenkaby.bikerental.rental.domain.exception;

import com.github.jenkaby.bikerental.shared.exception.BikeRentalException;
import lombok.Getter;

@Getter
public class PrepaymentRequiredException extends BikeRentalException {

    public static final String ERROR_CODE = "rental.prepayment.required";

    private static final String MESSAGE = "Prepayment must be received before starting rental";

    private final Long rentalId;

    public PrepaymentRequiredException(Long rentalId) {
        super(MESSAGE, ERROR_CODE);
        this.rentalId = rentalId;
    }
}
