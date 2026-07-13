package com.github.jenkaby.bikerental.tariff.infrastructure.persistence.entity;

import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.util.List;

public record QuoteResultSnapshot(
        List<Line> equipmentBreakdowns,
        BigDecimal subtotal,
        Discount discount,
        BigDecimal totalCost,
        Integer effectiveDurationMinutes,
        boolean estimate,
        boolean specialPricingApplied
) {

    public record Discount(
            BigDecimal percent,
            BigDecimal amount
    ) {
    }

    public record Line(
            @Nullable Long equipmentId,
            String equipmentType,
            Long tariffId,
            String tariffName,
            String pricingType,
            BigDecimal itemCost,
            Integer billedDurationMinutes,
            @Nullable Integer overtimeMinutes,
            @Nullable Integer forgivenMinutes,
            Breakdown calculationBreakdown
    ) {

        public record Breakdown(
                String breakdownPatternCode,
                String message,
                @Nullable Object params
        ) {
        }
    }
}
