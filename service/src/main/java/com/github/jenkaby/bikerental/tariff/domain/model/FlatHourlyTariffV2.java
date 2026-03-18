package com.github.jenkaby.bikerental.tariff.domain.model;

import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
import lombok.Getter;

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
}
