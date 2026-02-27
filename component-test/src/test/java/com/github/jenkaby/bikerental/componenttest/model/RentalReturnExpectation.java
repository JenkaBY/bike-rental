package com.github.jenkaby.bikerental.componenttest.model;

import java.math.BigDecimal;

public record RentalReturnExpectation(
        String status,
        BigDecimal baseCost,
        BigDecimal overtimeCost,
        BigDecimal finalCost,
        Integer actualMinutes,
        Integer plannedMinutes,
        Integer overtimeMinutes,
        Boolean forgivenessApplied,
        BigDecimal additionalPayment
) {
}
