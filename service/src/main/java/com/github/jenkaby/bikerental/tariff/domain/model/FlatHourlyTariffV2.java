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
public final class FlatHourlyTariffV2 extends TariffV2 {

    private final Money hourlyPrice;
    private final Duration minimumDuration;
    private final Money minimumDurationSurcharge;

    public FlatHourlyTariffV2(Long id, String name, String description, String equipmentTypeSlug,
                              String version, LocalDate validFrom, LocalDate validTo, TariffV2Status status,
                              Money hourlyPrice, Integer minimumDuration, Money minimumDurationSurcharge) {
        super(id, name, description, equipmentTypeSlug, PricingType.FLAT_HOURLY, version, validFrom, validTo, status);
        this.hourlyPrice = hourlyPrice;
        this.minimumDuration = Duration.ofMinutes(minimumDuration);
        this.minimumDurationSurcharge = minimumDurationSurcharge;
    }

    @Override
    public RentalCostV2 calculateCost(Duration duration) {
        if (isNegative(duration)) {
            return new BaseRentalCostV2(Money.zero(), new BreakdownCostDetails.Zero());
        }
        int minDuration = minimumDuration.toMinutesPart();
        Money surcharge = minimumDurationSurcharge;
        if (!isMoreThenMinimumDuration(duration)) {
            Money halfHourly = hourlyPrice.divide(2);
            Money cost = halfHourly.add(surcharge);
            String message = String.format("%dmin minimum: %s/2 + %s = %s", minDuration, hourlyPrice, surcharge, cost);
            return new BaseRentalCostV2(cost, new BreakdownCostDetails.FlatHourlyMinCost(message,
                    new BreakdownCostDetails.FlatHourlyMinCost.Details(minDuration, hourlyPrice.toString(), surcharge.toString(), cost.toString())));
        }
        long hours = duration.toHours();
        long minutes = duration.minusHours(hours).toMinutes();
        Money totalCost = hourlyPrice.multiply(BigDecimal.valueOf(hours));
        if (minutes > 0) {
            int intervals = getIntervalMinutes(minutes);
            Money perInterval = getRatePerMinInterval(hourlyPrice);
            totalCost = totalCost.add(perInterval.multiply(BigDecimal.valueOf(intervals)));
        }
        if (hours > 0) {
            String message = String.format("%dh %dmin flat: %d*%s + partial = %s", hours, minutes, hours, hourlyPrice, totalCost);
            return new BaseRentalCostV2(totalCost,  new BreakdownCostDetails.FlatHourlyStandard(message,
                    new BreakdownCostDetails.FlatHourlyStandard.Details((int) hours, (int) minutes, hourlyPrice.toString(), totalCost.toString()))
            );
        }
        String message = String.format("%dmin flat: %s", minutes, totalCost);
        return new BaseRentalCostV2(totalCost,
                new BreakdownCostDetails.FlatHourlyMinsOnly(message,
                        new BreakdownCostDetails.FlatHourlyMinsOnly.Details((int) minutes, totalCost.toString())));
    }

    private boolean isMoreThenMinimumDuration(Duration toBeVerified) {
        return toBeVerified.compareTo(minimumDuration) > 0;
    }
}
