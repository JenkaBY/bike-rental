package com.github.jenkaby.bikerental.tariff.domain.model;

import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
import com.github.jenkaby.bikerental.tariff.BreakdownCostDetails;
import com.github.jenkaby.bikerental.tariff.RentalCostV2;
import com.github.jenkaby.bikerental.tariff.domain.service.BaseRentalCostV2;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;

@Getter
public final class DailyTariffV2 extends TariffV2 {

    private final Money dailyPrice;
    private final Money overtimeHourlyPrice;

    public DailyTariffV2(Long id, String name, String description, String equipmentTypeSlug,
                         String version, LocalDate validFrom, LocalDate validTo, TariffV2Status status,
                         Money dailyPrice, Money overtimeHourlyPrice) {
        super(id, name, description, equipmentTypeSlug, PricingType.DAILY, version, validFrom, validTo, status);
        this.dailyPrice = dailyPrice;
        this.overtimeHourlyPrice = overtimeHourlyPrice;
    }

    @Override
    public RentalCostV2 calculateCost(Duration duration) {
        int durationMinutes = (int) duration.toMinutes();
        if (durationMinutes <= 0) {
            return new BaseRentalCostV2(Money.zero(), new BreakdownCostDetails.Zero());
        }
        if (durationMinutes <= MINUTES_PER_DAY) {
            String message = String.format("24h daily: %s", dailyPrice);
            return new BaseRentalCostV2(dailyPrice, new BreakdownCostDetails.DailyStandard(message,
                    new BreakdownCostDetails.DailyStandard.Details(dailyPrice.toString())));
        }
        int overtimeMinutes = durationMinutes - MINUTES_PER_DAY;
        int fullOvertimeHours = overtimeMinutes / MINUTES_PER_HOUR;
        int remainingOvertimeMin = overtimeMinutes % MINUTES_PER_HOUR;
        Money totalCost = dailyPrice.add(overtimeHourlyPrice.multiply(BigDecimal.valueOf(fullOvertimeHours)));
        if (remainingOvertimeMin > 0) {
            int intervals = remainingOvertimeMin / INTERVAL_MINUTES;
            Money perInterval = overtimeHourlyPrice.divide(INTERVALS_PER_HOUR);
            totalCost = totalCost.add(perInterval.multiply(BigDecimal.valueOf(intervals)));
        }
        String message = String.format("Daily + %dh %dmin overtime: %s + overtime = %s",
                fullOvertimeHours, remainingOvertimeMin, dailyPrice, totalCost);
        return new BaseRentalCostV2(totalCost, new BreakdownCostDetails.DailyOvertime(message,
                new BreakdownCostDetails.DailyOvertime.Details(fullOvertimeHours, remainingOvertimeMin, dailyPrice.toString(), totalCost.toString()))
        );
    }
}
