package com.github.jenkaby.bikerental.tariff.domain.model;

import com.github.jenkaby.bikerental.tariff.RentalCostV2;
import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.time.LocalDate;

@Getter
public sealed abstract class TariffV2
        permits DegressiveHourlyTariffV2, FlatHourlyTariffV2, DailyTariffV2, FlatFeeTariffV2, SpecialTariffV2 {

    protected static final int MINUTES_PER_DAY = 1440;
    protected static final int MINUTES_PER_HOUR = 60;
    protected static final int INTERVAL_MINUTES = 5;
    protected static final int INTERVALS_PER_HOUR = MINUTES_PER_HOUR / INTERVAL_MINUTES;
    protected static final int DEFAULT_MINIMUM_DURATION_MINUTES = 30;

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
}
