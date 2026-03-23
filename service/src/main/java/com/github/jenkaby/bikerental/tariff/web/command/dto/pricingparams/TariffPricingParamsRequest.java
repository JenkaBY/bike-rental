package com.github.jenkaby.bikerental.tariff.web.command.dto.pricingparams;

public sealed class TariffPricingParamsRequest permits DailyParamsRequest, DegressiveHourlyTariffParamsRequest, FlatFeeParamsRequest, FlatHourlyParamsRequest, SpecialTariffParamsRequest {
}
