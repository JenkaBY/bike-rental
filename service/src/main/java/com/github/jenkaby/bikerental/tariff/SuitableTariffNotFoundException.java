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

    private static final String MESSAGE_TEMPLATE = "No suitable tariff found for equipment type '%s' on date %s";

    private final String equipmentTypeSlug;
    private final LocalDate rentalDate;
    private final Duration duration;

    public SuitableTariffNotFoundException(String equipmentTypeSlug, LocalDate rentalDate) {
        super(MESSAGE_TEMPLATE.formatted(equipmentTypeSlug, rentalDate));
        this.equipmentTypeSlug = equipmentTypeSlug;
        this.rentalDate = rentalDate;
        this.duration = null;
    }

    public SuitableTariffNotFoundException(String equipmentTypeSlug, LocalDate rentalDate, Duration duration) {
        super(MESSAGE_TEMPLATE.formatted(equipmentTypeSlug, rentalDate) +
                (duration != null ? " for duration: " + duration.toMinutes() + " minutes" : ""));
        this.equipmentTypeSlug = equipmentTypeSlug;
        this.rentalDate = rentalDate;
        this.duration = duration;
    }
}
