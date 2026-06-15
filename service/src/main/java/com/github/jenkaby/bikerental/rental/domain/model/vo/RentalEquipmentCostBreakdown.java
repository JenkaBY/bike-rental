package com.github.jenkaby.bikerental.rental.domain.model.vo;

import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;

public record RentalEquipmentCostBreakdown(
        String pricingType,
        String tariffName,
        Integer billedDurationMinutes,
        @Nullable Integer overtimeMinutes,
        @Nullable Integer forgivenMinutes,
        BigDecimal itemCost,
        CalculationDetail calculationBreakdown
) {

    public record CalculationDetail(
            String breakdownPatternCode,
            String message,
            @Nullable Object params
    ) {
    }
}
