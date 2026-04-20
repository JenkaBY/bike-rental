package com.github.jenkaby.bikerental.tariff.web.query.dto;

import com.github.jenkaby.bikerental.tariff.BreakdownCostDetails;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Schema(description = "Selected V2 tariff with cost for the given duration")
public record TariffSelectionV2Response(
        @Schema(description = "Selected tariff") @NotNull TariffV2Response tariff,
        @Schema(description = "Total cost") @NotNull BigDecimal totalCost,
        @Schema(description = "Calculation breakdown") @NotNull BreakdownCostDetails calculationBreakdown
) {
}
