package com.github.jenkaby.bikerental.tariff.domain.model;

import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
import com.github.jenkaby.bikerental.tariff.RentalCostV2;
import com.github.jenkaby.bikerental.tariff.domain.service.BaseRentalCostV2Result;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;

@Getter
public final class FlatHourlyTariffV2 extends TariffV2 {

    private final Money hourlyPrice;
    private final Integer minimumDurationMinutes;
    private final Money minimumDurationSurcharge;

    public FlatHourlyTariffV2(Long id, String name, String description, String equipmentTypeSlug,
                              String version, LocalDate validFrom, LocalDate validTo, TariffV2Status status,
                              Money hourlyPrice, Integer minimumDurationMinutes, Money minimumDurationSurcharge) {
        super(id, name, description, equipmentTypeSlug, PricingType.FLAT_HOURLY, version, validFrom, validTo, status);
        this.hourlyPrice = hourlyPrice;
        this.minimumDurationMinutes = minimumDurationMinutes;
        this.minimumDurationSurcharge = minimumDurationSurcharge;
    }

    @Override
    public RentalCostV2 calculateCost(Duration duration) {
        int durationMinutes = (int) duration.toMinutes();
        if (durationMinutes <= 0) {
            return new BaseRentalCostV2Result(Money.zero(), "0 min: 0.00");
        }
        int minDuration = minimumDurationMinutes != null ? minimumDurationMinutes : DEFAULT_MINIMUM_DURATION_MINUTES;
        Money surcharge = minimumDurationSurcharge != null ? minimumDurationSurcharge : Money.zero();
        if (durationMinutes <= minDuration) {
            Money halfHourly = hourlyPrice.divide(2);
            Money cost = halfHourly.add(surcharge);
            String breakdown = String.format("%d min minimum: %s/2 + %s = %s",
                    minDuration, hourlyPrice, surcharge, cost);
            return new BaseRentalCostV2Result(cost, breakdown);
        }
        int fullHours = durationMinutes / MINUTES_PER_HOUR;
        int remainingMinutes = durationMinutes % MINUTES_PER_HOUR;
        Money totalCost = hourlyPrice.multiply(BigDecimal.valueOf(fullHours));
        if (remainingMinutes > 0) {
            int intervals = remainingMinutes / INTERVAL_MINUTES;
            Money perInterval = hourlyPrice.divide(INTERVALS_PER_HOUR);
            totalCost = totalCost.add(perInterval.multiply(BigDecimal.valueOf(intervals)));
        }
        String breakdown = fullHours > 0
                ? String.format("%dh %dmin flat: %d×%s + partial = %s", fullHours, remainingMinutes, fullHours, hourlyPrice, totalCost)
                : String.format("%dmin flat: %s", remainingMinutes, totalCost);
        return new BaseRentalCostV2Result(totalCost, breakdown);
    }
}
