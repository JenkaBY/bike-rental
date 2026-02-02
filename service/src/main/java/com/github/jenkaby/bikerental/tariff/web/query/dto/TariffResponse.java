package com.github.jenkaby.bikerental.tariff.web.query.dto;

import com.github.jenkaby.bikerental.tariff.domain.model.TariffStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TariffResponse(
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
        TariffStatus status
) {
}
