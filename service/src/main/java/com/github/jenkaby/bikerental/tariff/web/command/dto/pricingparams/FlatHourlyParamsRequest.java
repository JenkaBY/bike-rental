package com.github.jenkaby.bikerental.tariff.web.command.dto.pricingparams;

import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.RequiredArgsConstructor;

@Builder
@RequiredArgsConstructor
public final class FlatHourlyParamsRequest extends TariffPricingParamsRequest {
    @Positive
    private final int hourlyPrice;
    @Positive
    private final int minimumDurationMinutes;
    @Positive
    private final int minimumDurationSurcharge;
}
