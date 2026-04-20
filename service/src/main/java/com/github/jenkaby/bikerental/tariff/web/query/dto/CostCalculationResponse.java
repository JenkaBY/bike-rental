package com.github.jenkaby.bikerental.tariff.web.query.dto;

import com.github.jenkaby.bikerental.tariff.BreakdownCostDetails;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "Rental cost calculation result")
public record CostCalculationResponse(
        @NotNull List<EquipmentCostBreakdownResponse> equipmentBreakdowns,
        @Schema(description = "cost without discount applied")
        @NotNull BigDecimal subtotal,
        DiscountDetailResponse discount,
        @Schema(description = "cost with discount applied")
        @NotNull BigDecimal totalCost,
        @NotNull Integer effectiveDurationMinutes,
        boolean estimate,
        boolean specialPricingApplied
) {
    @Schema(description = "Per-equipment cost breakdown")
    public record EquipmentCostBreakdownResponse(
            @NotNull String equipmentType,
            @NotNull Long tariffId,
            @NotNull String tariffName,
            @NotNull String pricingType,
            @NotNull BigDecimal itemCost,
            @NotNull Integer billedDurationMinutes,
            Integer overtimeMinutes,
            Integer forgivenMinutes,
            @NotNull BreakdownCostDetails calculationBreakdown
    ) {
    }

    @Schema(description = "Discount applied")
    public record DiscountDetailResponse(
            @NotNull BigDecimal percent,
            @NotNull BigDecimal amount
    ) {
    }
}
