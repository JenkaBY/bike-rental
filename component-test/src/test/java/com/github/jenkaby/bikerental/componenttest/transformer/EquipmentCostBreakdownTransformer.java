package com.github.jenkaby.bikerental.componenttest.transformer;

import com.github.jenkaby.bikerental.rental.web.query.dto.EquipmentItemResponse;
import io.cucumber.java.DataTableType;

import java.util.Map;

public class EquipmentCostBreakdownTransformer {

    @DataTableType
    public EquipmentCostBreakdownAssertionHolder transform(Map<String, String> entry) {
        var equipmentId = DataTableHelper.toLong(entry, "equipmentId");
        var pricingType = DataTableHelper.getStringOrNull(entry, "pricingType");
        var tariffName = DataTableHelper.getStringOrNull(entry, "tariffName");
        var billedDurationMinutes = DataTableHelper.toInt(entry, "billedDurationMinutes");
        var overtimeMinutes = DataTableHelper.toInt(entry, "overtimeMinutes");
        var forgivenMinutes = DataTableHelper.toInt(entry, "forgivenMinutes");
        var itemCost = DataTableHelper.toBigDecimal(entry, "itemCost");
        var breakdownPatternCode = DataTableHelper.getStringOrNull(entry, "breakdownPatternCode");

        EquipmentItemResponse.CostBreakdown.CalculationDetail calculationDetail = breakdownPatternCode != null
                ? new EquipmentItemResponse.CostBreakdown.CalculationDetail(breakdownPatternCode, null, null)
                : null;
        var breakdown = new EquipmentItemResponse.CostBreakdown(
                pricingType, tariffName, billedDurationMinutes, overtimeMinutes, forgivenMinutes,
                itemCost, calculationDetail);
        return new EquipmentCostBreakdownAssertionHolder(equipmentId, breakdown);
    }

    public record EquipmentCostBreakdownAssertionHolder(
            Long equipmentId,
            EquipmentItemResponse.CostBreakdown breakdown
    ) {
    }
}
