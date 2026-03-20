package com.github.jenkaby.bikerental.componenttest.model;

import com.github.jenkaby.bikerental.tariff.domain.model.PricingType;
import com.github.jenkaby.bikerental.tariff.web.command.dto.TariffV2Request;
import com.github.jenkaby.bikerental.tariff.web.query.dto.PricingParams;

import java.time.LocalDate;


public record TariffV2RequestBuilder(
        String name,
        String description,
        String equipmentTypeSlug,
        PricingType pricingType,
        LocalDate validFrom,
        LocalDate validTo
) {

    public TariffV2Request toRequest(PricingParams params) {
        return new TariffV2Request(
                name,
                description,
                equipmentTypeSlug,
                pricingType,
                params,
                validFrom,
                validTo
        );
    }
}
