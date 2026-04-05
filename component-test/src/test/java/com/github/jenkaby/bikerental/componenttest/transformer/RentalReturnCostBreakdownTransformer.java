package com.github.jenkaby.bikerental.componenttest.transformer;

import com.github.jenkaby.bikerental.rental.web.command.dto.RentalReturnResponse;
import io.cucumber.java.DataTableType;

import java.util.Map;
import java.util.Optional;

public class RentalReturnCostBreakdownTransformer {

    @DataTableType
    public RentalReturnResponse.CostBreakdown costBreakdown(Map<String, String> entry) {
        var equipmentId = DataTableHelper.toLong(entry, "equipmentId");
        var baseCost = DataTableHelper.toBigDecimal(entry, "baseCost");
        var overtimeCost = DataTableHelper.toBigDecimal(entry, "overtimeCost");
        var totalCost = DataTableHelper.toBigDecimal(entry, "finalCost");
        if (DataTableHelper.toBigDecimal(entry, "totalCost") != null) {
            throw new IllegalArgumentException("totalCost must NOT be provided. It must be finalCost for CostBreakdown");
        }
        var actualMinutes = Optional.ofNullable(DataTableHelper.toInt(entry, "actualMinutes")).orElse(0);
        var billableMinutes = Optional.ofNullable(DataTableHelper.toInt(entry, "billableMinutes")).orElse(0);
        var plannedMinutes = Optional.ofNullable(DataTableHelper.toInt(entry, "plannedMinutes")).orElse(0);
        var overtimeMinutes = Optional.ofNullable(DataTableHelper.toInt(entry, "overtimeMinutes")).orElse(0);

        var forgivenessApplied = Optional.ofNullable(DataTableHelper.toBooleanOrNull(entry, "forgivenessApplied")).orElse(false);
        var calculationMessage = DataTableHelper.getStringOrNull(entry, "calculationMessage");

        return new RentalReturnResponse.CostBreakdown(
                equipmentId,
                baseCost,
                overtimeCost,
                totalCost,
                actualMinutes,
                billableMinutes,
                plannedMinutes,
                overtimeMinutes,
                forgivenessApplied,
                calculationMessage
        );
    }
}

