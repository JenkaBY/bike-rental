package com.github.jenkaby.bikerental.tariff.web.query.dto;

import com.github.jenkaby.bikerental.tariff.domain.model.TariffPeriod;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Result of tariff selection for a rental")
public record TariffSelectionResponse(
        @Schema(description = "Tariff ID", example = "3") Long id,
        @Schema(description = "Tariff name", example = "Standard Bike Rental") String name,
        @Schema(description = "Equipment type slug", example = "bike") String equipmentType,
        @Schema(description = "Price for the selected period", example = "180.00") BigDecimal price,
        @Schema(description = "Matched tariff period (e.g. HOUR, HALF_HOUR, DAY)") TariffPeriod period
) {
}
