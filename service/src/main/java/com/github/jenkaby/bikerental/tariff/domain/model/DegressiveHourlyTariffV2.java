package com.github.jenkaby.bikerental.tariff.domain.model;

import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
import lombok.Getter;

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
}
