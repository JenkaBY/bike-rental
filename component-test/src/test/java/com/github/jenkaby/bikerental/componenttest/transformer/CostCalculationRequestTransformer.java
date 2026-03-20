package com.github.jenkaby.bikerental.componenttest.transformer;

import com.github.jenkaby.bikerental.tariff.web.query.dto.CostCalculationRequest;
import io.cucumber.java.DataTableType;

import java.util.List;
import java.util.Map;

import static com.github.jenkaby.bikerental.componenttest.transformer.DataTableHelper.*;

public class CostCalculationRequestTransformer {

    @DataTableType
    public CostCalculationRequest transform(Map<String, String> entry) {
        var equipmentTypes = toStringList(entry, "equipmentTypes");
        List<CostCalculationRequest.EquipmentItemRequest> equipments = equipmentTypes.stream()
                .map(CostCalculationRequest.EquipmentItemRequest::new)
                .toList();

        var plannedDurationMinutes = toInt(entry, "plannedDurationMinutes");
        var actualDurationMinutes = toInt(entry, "actualDurationMinutes");
        var discountPercent = toInt(entry, "discountPercent");
        var specialTariffId = toLong(entry, "specialTariffId");
        var specialPrice = toBigDecimal(entry, "specialPrice");
        var rentalDate = toLocalDate(entry, "rentalDate");

        return new CostCalculationRequest(
                equipments,
                plannedDurationMinutes,
                actualDurationMinutes,
                discountPercent,
                specialTariffId,
                specialPrice,
                rentalDate
        );
    }
}

