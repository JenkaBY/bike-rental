package com.github.jenkaby.bikerental.tariff.web.query.dto;

import com.github.jenkaby.bikerental.tariff.BreakdownCostDetails;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Selected V2 tariff with cost for the given duration")
public record TariffSelectionV2Response(
        @Schema(description = "Selected tariff") TariffV2Response tariff,
        @Schema(description = "Total cost") BigDecimal totalCost,
        @Schema(description = "Calculation breakdown") BreakdownCostDetails calculationBreakdown
) {
}
