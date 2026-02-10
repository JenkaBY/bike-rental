package com.github.jenkaby.bikerental.tariff.web.query.dto;

import com.github.jenkaby.bikerental.tariff.domain.model.TariffPeriod;

import java.math.BigDecimal;

public record TariffSelectionResponse(
        Long id,
        String name,
        String equipmentType,
        BigDecimal price,
        TariffPeriod period
) {
}
