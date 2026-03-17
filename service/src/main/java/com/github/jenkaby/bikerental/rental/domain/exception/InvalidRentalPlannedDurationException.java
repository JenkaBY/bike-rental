package com.github.jenkaby.bikerental.rental.domain.exception;

import com.github.jenkaby.bikerental.shared.exception.BikeRentalException;
import lombok.Getter;

@Getter
public class InvalidRentalPlannedDurationException extends BikeRentalException {

    public static final String ERROR_CODE = "rental.planned-duration.invalid";

    private static final String MESSAGE_TEMPLATE = "Cannot perform operation on rental. Duration must present";

    public InvalidRentalPlannedDurationException(Long rentalId) {
        super(MESSAGE_TEMPLATE, ERROR_CODE, new RentalDetails(rentalId));
    }

    public RentalDetails getDetails() {
        return getParams()
                .map(d -> (RentalDetails) d)
                .orElseThrow(() -> new IllegalArgumentException("Expected RentalDetails in exception parameters"));
    }

    public record RentalDetails(Long rentalId) {
    }
}
