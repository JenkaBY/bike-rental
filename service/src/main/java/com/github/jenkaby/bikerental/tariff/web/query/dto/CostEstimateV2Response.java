package com.github.jenkaby.bikerental.tariff.web.query.dto;

import com.github.jenkaby.bikerental.tariff.BreakdownCostDetails;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Cost estimate for a single tariff and duration")
public record CostEstimateV2Response(
        @Schema(description = "Total cost") BigDecimal totalCost,
        @Schema(description = "Calculation breakdown") BreakdownCostDetails calculationBreakdown
) {
}
