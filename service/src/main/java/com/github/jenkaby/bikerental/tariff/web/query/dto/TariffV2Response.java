package com.github.jenkaby.bikerental.tariff.web.query.dto;

import com.github.jenkaby.bikerental.tariff.domain.model.PricingType;
import com.github.jenkaby.bikerental.tariff.domain.model.TariffV2Status;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

@Schema(description = "V2 Tariff details")
public record TariffV2Response(
        @NotNull Long id,
        @NotNull String name,
        String description,
        @NotNull String equipmentType,
        @NotNull PricingType pricingType,
        @NotNull PricingParams params,
        @NotNull LocalDate validFrom,
        LocalDate validTo,
        String version,
        @NotNull TariffV2Status status
) {
}
