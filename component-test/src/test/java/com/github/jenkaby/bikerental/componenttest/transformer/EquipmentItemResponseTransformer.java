package com.github.jenkaby.bikerental.componenttest.transformer;

import com.github.jenkaby.bikerental.rental.web.query.dto.EquipmentItemResponse;
import io.cucumber.java.DataTableType;

import java.util.Map;
import java.util.Optional;

public class EquipmentItemResponseTransformer {

    @DataTableType
    public EquipmentItemResponseTransformerHolder transform(Map<String, String> entry) {
        var idString = DataTableHelper.getStringOrNull(entry, "rentalId");
        var rentalId = Optional.ofNullable(idString)
                .map(Long::valueOf)
                .orElse(null);

        var equipmentId = DataTableHelper.toLong(entry, "equipmentId");
        var equipmentUid = DataTableHelper.getStringOrNull(entry, "equipmentUid");
        var tariffId = DataTableHelper.toLong(entry, "tariffId");
        var status = DataTableHelper.getStringOrNull(entry, "status");

        var estimatedCost = DataTableHelper.toBigDecimal(entry, "estimatedCost");
        var finalCost = DataTableHelper.toBigDecimal(entry, "finalCost");

        return new EquipmentItemResponseTransformerHolder(
                rentalId, new EquipmentItemResponse(
                equipmentId,
                equipmentUid,
                estimatedCost,
                finalCost,
                tariffId,
                status
        ));
    }

    public record EquipmentItemResponseTransformerHolder(Long rentalId, EquipmentItemResponse equipmentItemResponse) {
    }
}
