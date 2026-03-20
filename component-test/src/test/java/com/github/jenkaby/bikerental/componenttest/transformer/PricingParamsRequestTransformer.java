package com.github.jenkaby.bikerental.componenttest.transformer;

import com.github.jenkaby.bikerental.tariff.web.query.dto.PricingParams;
import io.cucumber.java.DataTableType;

import java.util.Map;

public class PricingParamsRequestTransformer {

    @DataTableType
    public PricingParams transform(Map<String, String> entry) {
        var firstHourPrice = DataTableHelper.toBigDecimal(entry, "firstHourPrice");
        var hourlyDiscount = DataTableHelper.toBigDecimal(entry, "hourlyDiscount");
        var minimumHourlyPrice = DataTableHelper.toBigDecimal(entry, "minimumHourlyPrice");
        var hourlyPrice = DataTableHelper.toBigDecimal(entry, "hourlyPrice");
        var dailyPrice = DataTableHelper.toBigDecimal(entry, "dailyPrice");
        var overtimeHourlyPrice = DataTableHelper.toBigDecimal(entry, "overtimeHourlyPrice");
        var issuanceFee = DataTableHelper.toBigDecimal(entry, "issuanceFee");
        var minimumDurationMinutes = DataTableHelper.toInt(entry, "minimumDurationMinutes");
        var minimumDurationSurcharge = DataTableHelper.toBigDecimal(entry, "minimumDurationSurcharge");
        var price = DataTableHelper.toBigDecimal(entry, "price");

        return new PricingParams(
                firstHourPrice,
                hourlyDiscount,
                minimumHourlyPrice,
                hourlyPrice,
                dailyPrice,
                overtimeHourlyPrice,
                issuanceFee,
                minimumDurationMinutes,
                minimumDurationSurcharge,
                price
        );
    }
}
