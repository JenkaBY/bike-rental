package com.github.jenkaby.bikerental.tariff.web.command.dto.pricingparams;

import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.RequiredArgsConstructor;

@Builder
@RequiredArgsConstructor
public final class DegressiveHourlyTariffParamsRequest extends TariffPricingParamsRequest {
    @Positive
    private final int firstHourPrice;
    @Positive
    private final int hourlyDiscount;
    @Positive
    private final int minimumHourlyPrice;
}
