package com.github.jenkaby.bikerental.tariff.domain.model;

import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
import lombok.Getter;

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
}
