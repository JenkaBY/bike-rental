package com.github.jenkaby.bikerental.tariff.application.usecase;

import com.github.jenkaby.bikerental.tariff.domain.model.Tariff;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface UpdateTariffUseCase {
    Tariff execute(UpdateTariffCommand command);

    record UpdateTariffCommand(
            Long id,
            String name,
            String description,
            String equipmentTypeSlug,
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
