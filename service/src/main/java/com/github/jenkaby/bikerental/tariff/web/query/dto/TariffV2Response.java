package com.github.jenkaby.bikerental.tariff.web.query.dto;

import com.github.jenkaby.bikerental.tariff.domain.model.PricingType;
import com.github.jenkaby.bikerental.tariff.domain.model.TariffV2Status;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "V2 Tariff details")
public record TariffV2Response(
        Long id,
        String name,
        String description,
        String equipmentType,
        PricingType pricingType,
        PricingParams params,
        LocalDate validFrom,
        LocalDate validTo,
        String version,
        TariffV2Status status
) {
}
