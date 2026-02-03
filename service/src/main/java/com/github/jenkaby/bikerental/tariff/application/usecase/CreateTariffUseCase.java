package com.github.jenkaby.bikerental.tariff.application.usecase;

import com.github.jenkaby.bikerental.tariff.domain.model.Tariff;
import com.github.jenkaby.bikerental.tariff.domain.model.TariffPeriod;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface CreateTariffUseCase {
    Tariff execute(CreateTariffCommand command);

    record CreateTariffCommand(
            String name,
            String description,
            String equipmentTypeSlug,
            TariffPeriod period,
            BigDecimal basePrice,
            BigDecimal halfHourPrice,
            BigDecimal hourPrice,
            BigDecimal dayPrice,
            BigDecimal hourDiscountedPrice,
            LocalDate validFrom,
            LocalDate validTo,
            String status
    ) {
    }
}
