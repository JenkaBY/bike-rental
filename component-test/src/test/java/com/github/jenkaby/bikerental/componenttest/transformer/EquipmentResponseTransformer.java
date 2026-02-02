package com.github.jenkaby.bikerental.componenttest.transformer;

import com.github.jenkaby.bikerental.equipment.web.query.dto.EquipmentResponse;
import io.cucumber.java.DataTableType;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

public class EquipmentResponseTransformer {

    @DataTableType
    public EquipmentResponse equipmentResponse(Map<String, String> entry) {
        var idString = DataTableHelper.getStringOrNull(entry, "id");
        var id = Optional.ofNullable(idString)
                .map(Long::parseLong)
                .orElse(null);

        var uid = DataTableHelper.getStringOrNull(entry, "uid");
        var equipmentTypeSlug = DataTableHelper.getStringOrNull(entry, "type");
        var statusSlug = DataTableHelper.getStringOrNull(entry, "status");
        var model = DataTableHelper.getStringOrNull(entry, "model");
        var commissionedAtString = DataTableHelper.getStringOrNull(entry, "commissionedAt");
        var commissionedAt = commissionedAtString != null ? LocalDate.parse(commissionedAtString) : null;
        var condition = DataTableHelper.getStringOrNull(entry, "condition");

        return new EquipmentResponse(
                id,
                entry.get("serialNumber"),
                uid,
                equipmentTypeSlug,
                statusSlug,
                model,
                commissionedAt,
                condition
        );
    }
}
