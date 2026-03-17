package com.github.jenkaby.bikerental.componenttest.transformer;

import com.github.jenkaby.bikerental.componenttest.model.RentalReturnExpectation;
import io.cucumber.java.DataTableType;

import java.util.Map;

public class RentalReturnResponseTransformer {

    @DataTableType
    public RentalReturnExpectation rentalReturnExpectation(Map<String, String> entry) {
        return new RentalReturnExpectation(
                DataTableHelper.getStringOrNull(entry, "status"),
                DataTableHelper.toBigDecimal(entry, "baseCost"),
                DataTableHelper.toBigDecimal(entry, "overtimeCost"),
                DataTableHelper.toBigDecimal(entry, "totalCost"),
                DataTableHelper.toInt(entry, "actualMinutes"),
                DataTableHelper.toInt(entry, "plannedMinutes"),
                DataTableHelper.toInt(entry, "overtimeMinutes"),
                DataTableHelper.toBooleanOrNull(entry, "forgivenessApplied"),
                DataTableHelper.toBigDecimal(entry, "additionalPayment")
        );
    }
}
