package com.github.jenkaby.bikerental.tariff.domain.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
public sealed abstract class TariffV2
        permits DegressiveHourlyTariffV2, FlatHourlyTariffV2,
        DailyTariffV2, FlatFeeTariffV2, SpecialTariffV2 {

    @Setter
    private Long id;
    private final String name;
    private final String description;
    private final String equipmentTypeSlug;
    private final PricingType pricingType;
    private final String version;
    private final LocalDate validFrom;
    private final LocalDate validTo;
    private TariffV2Status status;

    protected TariffV2(Long id, String name, String description, String equipmentTypeSlug,
                       PricingType pricingType, String version, LocalDate validFrom, LocalDate validTo,
                       TariffV2Status status) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.equipmentTypeSlug = equipmentTypeSlug;
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
}
