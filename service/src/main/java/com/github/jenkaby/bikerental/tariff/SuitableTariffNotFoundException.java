package com.github.jenkaby.bikerental.tariff;

import com.github.jenkaby.bikerental.shared.exception.BikeRentalException;
import lombok.Getter;

import java.time.Duration;
import java.time.LocalDate;

/**
 * Exception thrown when no suitable tariff can be found for the given criteria.
 * This is part of the public API of the tariff module and can be thrown by {@link TariffFacade}.
 */
@Getter
public class SuitableTariffNotFoundException extends BikeRentalException {

    public static final String ERROR_CODE = "tariff.suitable.not_found";

    private static final String MESSAGE_TEMPLATE = "No suitable tariff found for equipment type '%s' on date %s";

    public SuitableTariffNotFoundException(String equipmentTypeSlug, LocalDate rentalDate, Duration duration) {
        super(MESSAGE_TEMPLATE.formatted(equipmentTypeSlug, rentalDate) +
                        (duration != null ? " for duration: " + duration.toMinutes() + " minutes" : ""), ERROR_CODE,
                new Details(equipmentTypeSlug, rentalDate, duration));
    }

    public Details getDetails() {
        return getParams().map(params -> (Details) params)
                .orElseThrow(() -> new IllegalArgumentException("Expected Details in exception parameters"));
    }

    public record Details(String equipmentType, LocalDate rentalDate, Duration duration) {
    }
}
