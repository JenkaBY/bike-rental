package com.github.jenkaby.bikerental.tariff.application.strategy;

import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
import com.github.jenkaby.bikerental.tariff.domain.model.TariffPeriod;
import org.jspecify.annotations.NonNull;

import java.math.BigDecimal;
import java.math.RoundingMode;


public class ProportionalOvertimeCalculationStrategy implements OvertimeCalculationStrategy {

    private final int roundingIntervalMinutes;


    public ProportionalOvertimeCalculationStrategy(int roundingIntervalMinutes) {
        if (roundingIntervalMinutes <= 0) {
            throw new IllegalArgumentException("Rounding interval must be positive");
        }
        this.roundingIntervalMinutes = roundingIntervalMinutes;
    }

    @Override
    public @NonNull Money calculateOvertimeCost(
            @NonNull Money basePrice,
            @NonNull TariffPeriod period,
            int overtimeMinutes,
            int forgivenMinutes) {

        // Chargeable overtime (beyond forgiven minutes)
        int chargeableOvertime = overtimeMinutes - forgivenMinutes;

        if (chargeableOvertime <= 0) {
            return Money.zero();
        }

        // Calculate price per rounding interval based on period
        long periodMinutes = period.getMinutes();
        BigDecimal pricePerInterval = basePrice.amount()
                .divide(BigDecimal.valueOf(periodMinutes), 10, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(roundingIntervalMinutes));

        int intervals = (chargeableOvertime + roundingIntervalMinutes - 1) / roundingIntervalMinutes;

        BigDecimal totalOvertime = pricePerInterval.multiply(BigDecimal.valueOf(intervals));

        return Money.of(totalOvertime);
    }
}
