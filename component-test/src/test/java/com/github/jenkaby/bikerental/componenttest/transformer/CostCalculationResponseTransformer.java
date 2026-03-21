package com.github.jenkaby.bikerental.componenttest.transformer;

import com.github.jenkaby.bikerental.componenttest.config.support.BreakdownCostDetailsDeserializer;
import com.github.jenkaby.bikerental.tariff.BreakdownCostDetails;
import com.github.jenkaby.bikerental.tariff.web.query.dto.CostCalculationResponse;
import io.cucumber.java.DataTableType;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

import static com.github.jenkaby.bikerental.componenttest.transformer.DataTableHelper.*;

public class CostCalculationResponseTransformer {

    @DataTableType
    public CostCalculationResponse transform(Map<String, String> entry) {
        var subtotal = toBigDecimal(entry, "subtotal");
        var totalCost = toBigDecimal(entry, "totalCost");
        var effectiveDurationMinutes = toInt(entry, "effectiveDurationMinutes");
        var estimate = Optional.ofNullable(toBooleanOrNull(entry, "estimate")).orElse(false);
        var specialPricingApplied = Optional.ofNullable(toBooleanOrNull(entry, "specialPricingApplied")).orElse(false);

        var discountPercent = toBigDecimal(entry, "discountPercent");
        var discountAmount = toBigDecimal(entry, "discountAmount");
        CostCalculationResponse.DiscountDetailResponse discount = discountPercent != null || discountAmount != null
                ? new CostCalculationResponse.DiscountDetailResponse(discountPercent, discountAmount)
                : new CostCalculationResponse.DiscountDetailResponse(BigDecimal.ZERO, BigDecimal.ZERO);

        return new CostCalculationResponse(
                null,
                subtotal,
                discount,
                totalCost,
                effectiveDurationMinutes,
                estimate,
                specialPricingApplied
        );
    }

    @DataTableType
    public CostCalculationResponse.EquipmentCostBreakdownResponse breakdownResponse(Map<String, String> entry) {
        var equipmentType = getStringOrNull(entry, "equipmentType");
        var tariffId = toLong(entry, "tariffId");
        var tariffName = getStringOrNull(entry, "tariffName");
        var pricingType = getStringOrNull(entry, "pricingType");
        var itemCost = toBigDecimal(entry, "itemCost");
        var billedDurationMinutes = toInt(entry, "billedDuration");
        var overtimeMinutes = toInt(entry, "overtimeMinutes");
        var forgivenMinutes = toInt(entry, "forgivenMinutes");
        var pattern = DataTableHelper.getStringOrNull(entry, "pattern");
        var message = DataTableHelper.getStringOrNull(entry, "message");

        return new CostCalculationResponse.EquipmentCostBreakdownResponse(
                equipmentType,
                tariffId,
                tariffName,
                pricingType,
                itemCost,
                billedDurationMinutes,
                overtimeMinutes,
                forgivenMinutes,
                new BreakdownCostDetailsDeserializer.TestBreakdownCostDetails(pattern, message, null)
        );
    }

    public BreakdownCostDetails transformToBreakdownCostDetails(Map<String, String> entry) {
        var pattern = DataTableHelper.getStringOrNull(entry, "pattern");
        var message = DataTableHelper.getStringOrNull(entry, "message");

        return new BreakdownCostDetailsDeserializer.TestBreakdownCostDetails(
                pattern,
                message,
                Map.of()
        );
    }
}

