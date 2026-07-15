package com.github.jenkaby.bikerental.componenttest.transformer;

import com.github.jenkaby.bikerental.componenttest.model.CostCalculationV2RequestBuilder;
import com.github.jenkaby.bikerental.tariff.web.query.dto.CostCalculationV2Request;
import io.cucumber.java.DataTableType;

import java.util.Map;

import static com.github.jenkaby.bikerental.componenttest.transformer.DataTableHelper.*;

public class CostCalculationV2RequestTransformer {

    @DataTableType
    public CostCalculationV2RequestBuilder transform(Map<String, String> entry) {
        var startAt = parseLocalDateTimeToInstant(entry, "startAt");
        var plannedDurationMinutes = toInt(entry, "plannedDurationMinutes");
        var discountPercent = toInt(entry, "discountPercent");
        var specialTariffId = toLong(entry, "specialTariffId");
        var specialPrice = toBigDecimal(entry, "specialPrice");

        return new CostCalculationV2RequestBuilder(
                startAt,
                plannedDurationMinutes,
                discountPercent,
                specialTariffId,
                specialPrice
        );
    }

    @DataTableType
    public CostCalculationV2Request.EquipmentItemRequest equipmentItem(Map<String, String> entry) {
        return new CostCalculationV2Request.EquipmentItemRequest(
                toLong(entry, "equipmentId"),
                getStringOrNull(entry, "equipmentType"),
                parseLocalDateTimeToInstant(entry, "startAt"),
                parseLocalDateTimeToInstant(entry, "returnAt")
        );
    }
}
