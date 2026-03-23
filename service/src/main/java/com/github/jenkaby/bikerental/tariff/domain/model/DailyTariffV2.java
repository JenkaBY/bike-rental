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
        if (isNegative(duration)) {
            return new BaseRentalCostV2(Money.zero(), new BreakdownCostDetails.Zero());
        }
        long days = duration.toDays();
        Duration remainder = duration.minusDays(days);
        long hours = remainder.toHours();
        long minutes = remainder.minusHours(hours).toMinutes();
        Money total;
        if (days == 0) {
            total = dailyPrice;
            String message = String.format("1d = %s", total);
            return new BaseRentalCostV2(total,
                    new BreakdownCostDetails.DailyStandard(message,
                            new BreakdownCostDetails.DailyStandard.Details(1, total.toString())
                    )
            );
        }
        total = dailyPrice.multiply(BigDecimal.valueOf(days));
        if (hours > 0) {
            total = total.add(overtimeHourlyPrice.multiply(BigDecimal.valueOf(hours)));
        }
        if (minutes > 0) {
            int intervals = getIntervalMinutes(minutes);
            Money perInterval = getRatePerMinInterval(overtimeHourlyPrice);
            total = total.add(perInterval.multiply(BigDecimal.valueOf(intervals)));
        }
        if (hours == 0 && minutes == 0) {
            String message = String.format("%dd = %s", days, total);
            return new BaseRentalCostV2(total,
                    new BreakdownCostDetails.DailyStandard(message,
                            new BreakdownCostDetails.DailyStandard.Details((int) days, total.toString())
                    )
            );
        } else {
            String message = String.format("%dd + %dh %dmin = %s", days, hours, minutes, total);
            return new BaseRentalCostV2(total,
                    new BreakdownCostDetails.DailyOvertime(message,
                            new BreakdownCostDetails.DailyOvertime.Details((int) days, (int) hours, (int) minutes, total.toString())
                    )
            );
        }
    }
}
