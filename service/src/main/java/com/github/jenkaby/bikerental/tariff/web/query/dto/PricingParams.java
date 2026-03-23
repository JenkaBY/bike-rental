package com.github.jenkaby.bikerental.tariff.web.query.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Pricing-type-specific parameters (only fields relevant to the tariff's pricing type are populated)")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PricingParams(
        BigDecimal firstHourPrice,
        BigDecimal hourlyDiscount,
        BigDecimal minimumHourlyPrice,
        BigDecimal hourlyPrice,
        BigDecimal dailyPrice,
        BigDecimal overtimeHourlyPrice,
        BigDecimal issuanceFee,
        Integer minimumDurationMinutes,
        BigDecimal minimumDurationSurcharge,
        BigDecimal price
) {
}
