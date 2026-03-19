package com.github.jenkaby.bikerental.tariff.domain.model;

import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
import com.github.jenkaby.bikerental.tariff.RentalCostV2;
import com.github.jenkaby.bikerental.tariff.domain.service.BaseRentalCostV2Result;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;

@Getter
public final class DegressiveHourlyTariffV2 extends TariffV2 {

    private final Money firstHourPrice;
    private final Money hourlyDiscount;
    private final Money minimumHourlyPrice;
    private final Integer minimumDurationMinutes;
    private final Money minimumDurationSurcharge;

    public DegressiveHourlyTariffV2(Long id, String name, String description, String equipmentTypeSlug,
                                    String version, LocalDate validFrom, LocalDate validTo, TariffV2Status status,
                                    Money firstHourPrice, Money hourlyDiscount, Money minimumHourlyPrice,
                                    Integer minimumDurationMinutes, Money minimumDurationSurcharge) {
        super(id, name, description, equipmentTypeSlug, PricingType.DEGRESSIVE_HOURLY, version, validFrom, validTo, status);
        this.firstHourPrice = firstHourPrice;
        this.hourlyDiscount = hourlyDiscount;
        this.minimumHourlyPrice = minimumHourlyPrice;
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
            Money halfFirst = firstHourPrice.divide(2);
            Money cost = halfFirst.add(surcharge);
            String breakdown = String.format("%d min minimum: %s/2 + %s = %s", minDuration, firstHourPrice, surcharge, cost);
            return new BaseRentalCostV2Result(cost, breakdown);
        }
        int fullHours = durationMinutes / MINUTES_PER_HOUR;
        int remainingMinutes = durationMinutes % MINUTES_PER_HOUR;
        Money totalCost = Money.zero();
        StringBuilder breakdownBuilder = new StringBuilder();
        for (int hour = 1; hour <= fullHours; hour++) {
            Money rate = rateForHour(hour);
            totalCost = totalCost.add(rate);
            if (hour > 1) {
                breakdownBuilder.append("+");
            }
            breakdownBuilder.append(rate);
        }
        if (remainingMinutes > 0) {
            Money nextHourRate = rateForHour(fullHours + 1);
            int intervals = remainingMinutes / INTERVAL_MINUTES;
            Money perInterval = nextHourRate.divide(INTERVALS_PER_HOUR);
            Money remainingCost = perInterval.multiply(BigDecimal.valueOf(intervals));
            totalCost = totalCost.add(remainingCost);
            if (!breakdownBuilder.isEmpty()) {
                breakdownBuilder.append("+");
            }
            breakdownBuilder.append(intervals).append("×(").append(nextHourRate).append("/12)");
        }
        String desc = fullHours > 0
                ? String.format("%dh %dmin degressive: %s = %s", fullHours, remainingMinutes, breakdownBuilder, totalCost)
                : String.format("%dmin degressive: %s = %s", remainingMinutes, breakdownBuilder, totalCost);
        return new BaseRentalCostV2Result(totalCost, desc);
    }

    private Money rateForHour(int hour) {
        Money base = firstHourPrice.subtract(hourlyDiscount.multiply(BigDecimal.valueOf(hour - 1)));
        if (base.compareTo(minimumHourlyPrice) < 0) {
            return minimumHourlyPrice;
        }
        return base;
    }
}
