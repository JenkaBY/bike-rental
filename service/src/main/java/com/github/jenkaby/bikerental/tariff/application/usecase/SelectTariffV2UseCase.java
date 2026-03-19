package com.github.jenkaby.bikerental.tariff.application.usecase;

import com.github.jenkaby.bikerental.tariff.domain.model.TariffV2;

import java.time.Duration;
import java.time.LocalDate;

public interface SelectTariffV2UseCase {

    TariffV2 execute(SelectTariffCommand command);

    record SelectTariffCommand(String equipmentTypeSlug, Duration duration, LocalDate rentalDate) {
    }
}
