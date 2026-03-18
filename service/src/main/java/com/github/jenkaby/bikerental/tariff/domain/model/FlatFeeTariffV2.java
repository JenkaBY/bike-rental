package com.github.jenkaby.bikerental.tariff.domain.model;

import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public final class FlatFeeTariffV2 extends TariffV2 {

    private final Money issuanceFee;

    public FlatFeeTariffV2(Long id, String name, String description, String equipmentTypeSlug,
                           String version, LocalDate validFrom, LocalDate validTo, TariffV2Status status,
                           Money issuanceFee) {
        super(id, name, description, equipmentTypeSlug, PricingType.FLAT_FEE, version, validFrom, validTo, status);
        this.issuanceFee = issuanceFee;
    }
}
