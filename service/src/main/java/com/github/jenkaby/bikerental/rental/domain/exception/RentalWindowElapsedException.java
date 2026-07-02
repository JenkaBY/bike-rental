package com.github.jenkaby.bikerental.rental.domain.exception;

import com.github.jenkaby.bikerental.shared.exception.BikeRentalException;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class RentalWindowElapsedException extends BikeRentalException {

    public static final String ERROR_CODE = "rental.window.elapsed";

    private static final String MESSAGE_TEMPLATE = "Cannot add equipment to rental %s: rental window has elapsed. Expected return at %s, now %s";

    public RentalWindowElapsedException(Long rentalId, LocalDateTime expectedReturnAt, LocalDateTime now) {
        super(MESSAGE_TEMPLATE.formatted(rentalId, expectedReturnAt, now), ERROR_CODE,
                new RentalWindowDetails(rentalId, expectedReturnAt, now));
    }

    public RentalWindowDetails getDetails() {
        return getParams()
                .map(d -> (RentalWindowDetails) d)
                .orElseThrow(() -> new IllegalArgumentException("Expected RentalWindowDetails in exception parameters"));
    }

    public record RentalWindowDetails(Long rentalId, LocalDateTime expectedReturnAt, LocalDateTime now) {
    }
}
