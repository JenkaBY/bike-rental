package com.github.jenkaby.bikerental.rental.domain.exception;

import com.github.jenkaby.bikerental.shared.exception.BikeRentalException;
import com.github.jenkaby.bikerental.shared.web.advice.ErrorCodes;
import lombok.Getter;

@Getter
public class HoldRequiredException extends BikeRentalException {

    private static final String MESSAGE = "A fund hold must exist before the rental can be activated";

    public HoldRequiredException(Long rentalId) {
        super(MESSAGE, ErrorCodes.HOLD_REQUIRED, new HoldRequiredDetails(rentalId));
    }

    public HoldRequiredDetails getDetails() {
        return getParams()
                .map(d -> (HoldRequiredDetails) d)
                .orElseThrow(() -> new IllegalArgumentException("Expected HoldRequiredDetails in exception parameters"));
    }

    public record HoldRequiredDetails(Long rentalId) {
    }
}
