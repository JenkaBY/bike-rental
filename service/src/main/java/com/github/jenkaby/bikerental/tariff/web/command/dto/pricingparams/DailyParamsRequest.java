package com.github.jenkaby.bikerental.tariff.web.command.dto.pricingparams;

import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.RequiredArgsConstructor;

@Builder
@RequiredArgsConstructor
public final class DailyParamsRequest extends TariffPricingParamsRequest {
    @Positive
    private final int dailyPrice;
    @Positive
    private final int overtimeHourlyPrice;
}
