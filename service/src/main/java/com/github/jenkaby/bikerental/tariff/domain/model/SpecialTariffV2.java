package com.github.jenkaby.bikerental.tariff.domain.model;

import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public final class SpecialTariffV2 extends TariffV2 {

    private final Money price;

    public SpecialTariffV2(Long id, String name, String description, String equipmentTypeSlug,
                           String version, LocalDate validFrom, LocalDate validTo, TariffV2Status status,
                           Money price) {
        super(id, name, description, equipmentTypeSlug, PricingType.SPECIAL, version, validFrom, validTo, status);
        this.price = price;
    }
}
