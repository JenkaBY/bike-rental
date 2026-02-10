package com.github.jenkaby.bikerental.tariff.application.usecase;

import com.github.jenkaby.bikerental.tariff.domain.model.Tariff;

import java.time.LocalDate;

public interface SelectTariffForRentalUseCase {
    Tariff execute(SelectTariffCommand command);

    record SelectTariffCommand(
            String equipmentType,
            int durationMinutes,
            LocalDate rentalDate
    ) {
    }
}
