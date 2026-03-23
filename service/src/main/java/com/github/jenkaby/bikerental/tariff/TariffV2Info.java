package com.github.jenkaby.bikerental.tariff;

import com.github.jenkaby.bikerental.tariff.domain.model.PricingType;

import java.time.LocalDate;

public record TariffV2Info(
        Long id,
        String name,
        String description,
        String equipmentType,
        PricingType pricingType,
        String version,
        LocalDate validFrom,
        LocalDate validTo
) {
}
