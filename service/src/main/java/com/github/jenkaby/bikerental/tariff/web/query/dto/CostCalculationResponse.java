package com.github.jenkaby.bikerental.tariff.web.query.dto;

import com.github.jenkaby.bikerental.tariff.BreakdownCostDetails;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "Rental cost calculation result")
public record CostCalculationResponse(
        List<EquipmentCostBreakdownResponse> equipmentBreakdowns,
        BigDecimal subtotal,
        DiscountDetailResponse discount,
        BigDecimal totalCost,
        Integer effectiveDurationMinutes,
        boolean estimate,
        boolean specialPricingApplied
) {
    @Schema(description = "Per-equipment cost breakdown")
    public record EquipmentCostBreakdownResponse(
            String equipmentType,
            Long tariffId,
            String tariffName,
            String pricingType,
            BigDecimal itemCost,
            Integer billedDurationMinutes,
            Integer overtimeMinutes,
            Integer forgivenMinutes,
            BreakdownCostDetails calculationBreakdown
    ) {
    }

    @Schema(description = "Discount applied")
    public record DiscountDetailResponse(
            BigDecimal percent,
            BigDecimal amount
    ) {
    }
}
