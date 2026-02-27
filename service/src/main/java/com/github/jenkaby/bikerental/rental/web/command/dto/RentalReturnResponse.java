package com.github.jenkaby.bikerental.rental.web.command.dto;

import com.github.jenkaby.bikerental.finance.PaymentInfo;
import com.github.jenkaby.bikerental.rental.web.query.dto.RentalResponse;

import java.math.BigDecimal;

public record RentalReturnResponse(
        RentalResponse rental,
        CostBreakdown cost,
        BigDecimal additionalPayment,
        PaymentInfo paymentInfo
) {
    public record CostBreakdown(
            BigDecimal baseCost,
            BigDecimal overtimeCost,
            BigDecimal totalCost,
            int actualMinutes,
            int billableMinutes,
            int plannedMinutes,
            int overtimeMinutes,
            boolean forgivenessApplied,
            String calculationMessage
    ) {
    }
}
