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
        if (durationMinutes < MINUTES_PER_DAY) {
            Money total = dailyPrice;
            String message = String.format("1d = %s", total);
            return new BaseRentalCostV2(total,
                    new BreakdownCostDetails.DailyStandard(message,
                            new BreakdownCostDetails.DailyStandard.Details(1, total.toString())
                    )
            );
        }
        int days = durationMinutes / MINUTES_PER_DAY;
        int remainder = durationMinutes % MINUTES_PER_DAY;
        int hours = remainder / MINUTES_PER_HOUR;
        int minutes = remainder % MINUTES_PER_HOUR;
        Money total = dailyPrice.multiply(BigDecimal.valueOf(days));
        if (hours > 0) {
            total = total.add(overtimeHourlyPrice.multiply(BigDecimal.valueOf(hours)));
        }
        if (minutes > 0) {
            int intervals = minutes / INTERVAL_MINUTES;
            Money perInterval = overtimeHourlyPrice.divide(INTERVALS_PER_HOUR);
            total = total.add(perInterval.multiply(BigDecimal.valueOf(intervals)));
        }
        if (hours == 0 && minutes == 0) {
            String message = String.format("%dd = %s", days, total);
            return new BaseRentalCostV2(total,
                    new BreakdownCostDetails.DailyStandard(message,
                            new BreakdownCostDetails.DailyStandard.Details(days, total.toString())
                    )
            );
        } else {
            String message = String.format("%dd + %dh %dmin = %s", days, hours, minutes, total);
            return new BaseRentalCostV2(total,
                    new BreakdownCostDetails.DailyOvertime(message,
                            new BreakdownCostDetails.DailyOvertime.Details(days, hours, minutes, total.toString())
                    )
            );
        }
    }
}
