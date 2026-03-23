package com.github.jenkaby.bikerental.tariff.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PricingType {
    DEGRESSIVE_HOURLY("tariff.pricing-type.degressive-hourly.title",
            "tariff.pricing-type.degressive-hourly.description"),
    FLAT_HOURLY("tariff.pricing-type.flat-hourly.title",
            "tariff.pricing-type.flat-hourly.description"),
    DAILY("tariff.pricing-type.daily.title",
            "tariff.pricing-type.daily.description"),
    FLAT_FEE("tariff.pricing-type.flat-fee.title",
            "tariff.pricing-type.flat-fee.description"),
    SPECIAL("tariff.pricing-type.special.title",
            "tariff.pricing-type.special.description");

    private final String codeTitle;
    private final String codeDescription;
}
