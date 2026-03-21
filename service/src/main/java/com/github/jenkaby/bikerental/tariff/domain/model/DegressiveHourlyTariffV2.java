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
public final class DegressiveHourlyTariffV2 extends TariffV2 {

    private final Money firstHourPrice;
    private final Money hourlyDiscount;
    private final Money minimumHourlyPrice;
    private final Duration minimumDuration;
    private final Money minimumDurationSurcharge;

    public DegressiveHourlyTariffV2(Long id, String name, String description, String equipmentTypeSlug,
                                    String version, LocalDate validFrom, LocalDate validTo, TariffV2Status status,
                                    Money firstHourPrice, Money hourlyDiscount, Money minimumHourlyPrice,
                                    Integer minimumDuration, Money minimumDurationSurcharge) {
        super(id, name, description, equipmentTypeSlug, PricingType.DEGRESSIVE_HOURLY, version, validFrom, validTo, status);
        this.firstHourPrice = firstHourPrice;
        this.hourlyDiscount = hourlyDiscount;
        this.minimumHourlyPrice = minimumHourlyPrice;
        this.minimumDuration = Duration.ofMinutes(minimumDuration);
        this.minimumDurationSurcharge = minimumDurationSurcharge;
    }

    @Override
    public RentalCostV2 calculateCost(Duration duration) {
        if (isNegative(duration)) {
            return new BaseRentalCostV2(Money.zero(), new BreakdownCostDetails.Zero());
        }
        if (!isMoreThenMinimumDuration(duration)) {
            Money surcharge = minimumDurationSurcharge;
            long minDuration = minimumDuration.toMinutes();
            Money halfFirst = firstHourPrice.divide(2);
            Money cost = halfFirst.add(surcharge);
            String message = String.format("%dmin minimum: %s/2 + %s = %s", minDuration, firstHourPrice, surcharge, cost);
            return new BaseRentalCostV2(cost,
                    new BreakdownCostDetails.DegressiveHourlyMin(message,
                            new BreakdownCostDetails.DegressiveHourlyMin.Details(minDuration, firstHourPrice.toString(), surcharge.toString(), cost.toString())));
        }
        long hours = duration.toHours();
        long minutes = duration.minusHours(hours).toMinutes();
        Money totalCost = Money.zero();
        StringBuilder breakdownBuilder = new StringBuilder();
        for (int hour = 1; hour <= hours; hour++) {
            Money rate = rateForHour(hour);
            totalCost = totalCost.add(rate);
            if (hour > 1) {
                breakdownBuilder.append("+");
            }
            breakdownBuilder.append(rate);
        }
        if (minutes > 0) {
            Money nextHourRate = rateForHour((int) hours + 1);
            int intervals = getIntervalMinutes(minutes);
            Money perInterval = getRatePerMinInterval(nextHourRate);
            Money remainingCost = perInterval.multiply(BigDecimal.valueOf(intervals));
            totalCost = totalCost.add(remainingCost);
            if (!breakdownBuilder.isEmpty()) {
                breakdownBuilder.append("+");
            }
            breakdownBuilder.append(intervals).append("*(").append(nextHourRate).append("/12)");
        }
        if (hours > 0) {
            String message = String.format("%dh %dmin degressive: %s = %s", hours, minutes, breakdownBuilder, totalCost);
            return new BaseRentalCostV2(totalCost,
                    new BreakdownCostDetails.DegressiveHourlyStandard(message,
                            new BreakdownCostDetails.DegressiveHourlyStandard.Details(hours, minutes, breakdownBuilder.toString(), totalCost.toString()))
            );
        }
        String message = String.format("%dmin degressive: %s = %s", minutes, breakdownBuilder, totalCost);
        return new BaseRentalCostV2(totalCost,
                new BreakdownCostDetails.DegressiveHourlyMinutesOnly(message,
                        new BreakdownCostDetails.DegressiveHourlyMinutesOnly.Details(minutes, breakdownBuilder.toString(), totalCost.toString()))
        );
    }

    private Money rateForHour(int hour) {
        Money base = firstHourPrice.subtract(hourlyDiscount.multiply(BigDecimal.valueOf(hour - 1)));
        if (base.compareTo(minimumHourlyPrice) < 0) {
            return minimumHourlyPrice;
        }
        return base;
    }

    private boolean isMoreThenMinimumDuration(Duration toBeVerified) {
        return toBeVerified.compareTo(minimumDuration) > 0;
    }
}
