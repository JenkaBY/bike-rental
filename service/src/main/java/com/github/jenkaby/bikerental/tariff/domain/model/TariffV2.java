package com.github.jenkaby.bikerental.tariff.domain.model;

import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
import com.github.jenkaby.bikerental.tariff.RentalCostV2;
import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.time.LocalDate;

@Getter
public sealed abstract class TariffV2
        permits DegressiveHourlyTariffV2, FlatHourlyTariffV2, DailyTariffV2, FlatFeeTariffV2, SpecialTariffV2 {


    private static final int INTERVAL_MINUTES = 5;
    private static final int INTERVALS_PER_HOUR = (int) Duration.ofHours(1).toMinutes() / INTERVAL_MINUTES;

    @Setter
    private Long id;
    private final String name;
    private final String description;
    private final String equipmentType;
    private final PricingType pricingType;
    private final String version;
    private final LocalDate validFrom;
    private final LocalDate validTo;
    private TariffV2Status status;

    protected TariffV2(Long id, String name, String description, String equipmentType,
                       PricingType pricingType, String version, LocalDate validFrom, LocalDate validTo,
                       TariffV2Status status) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.equipmentType = equipmentType;
        this.pricingType = pricingType;
        this.version = version;
        this.validFrom = validFrom;
        this.validTo = validTo;
        this.status = status;
    }

    public boolean isActive() {
        return TariffV2Status.ACTIVE == status;
    }

    public void activate() {
        this.status = TariffV2Status.ACTIVE;
    }

    public void deactivate() {
        this.status = TariffV2Status.INACTIVE;
    }

    public boolean isValidOn(LocalDate date) {
        boolean afterStart = !date.isBefore(validFrom);
        boolean beforeEnd = validTo == null || !date.isAfter(validTo);
        return afterStart && beforeEnd;
    }

    public abstract RentalCostV2 calculateCost(Duration duration);

    protected static boolean isNegative(Duration duration) {
        return duration == null || duration.isZero() || duration.isNegative();
    }

    protected static int getNumberOfDays(Duration duration) {
        return (int) Math.ceil((double) duration.toMinutes() / Duration.ofDays(1).toMinutes());
    }

    protected static int getIntervalMinutes(long minutes) {
        return (int) (minutes / INTERVAL_MINUTES);
    }

    protected static Money getRatePerMinInterval(Money ratePerHour) {
        return ratePerHour.divide(INTERVALS_PER_HOUR);
    }
}
