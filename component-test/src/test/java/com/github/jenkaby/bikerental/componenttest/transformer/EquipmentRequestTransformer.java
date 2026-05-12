package com.github.jenkaby.bikerental.componenttest.transformer;

import com.github.jenkaby.bikerental.equipment.web.command.dto.EquipmentRequest;
import io.cucumber.java.DataTableType;

import java.time.LocalDate;
import java.util.Map;

public class EquipmentRequestTransformer {

    @DataTableType
    public EquipmentRequest equipmentRequest(Map<String, String> entry) {
        var uid = DataTableHelper.getStringOrNull(entry, "uid");
        var equipmentTypeSlug = DataTableHelper.getStringOrNull(entry, "type");
        var statusSlug = DataTableHelper.getStringOrNull(entry, "status");
        var model = DataTableHelper.getStringOrNull(entry, "model");
        var commissionedAtString = DataTableHelper.getStringOrNull(entry, "commissionedAt");
        var commissionedAt = commissionedAtString != null ? LocalDate.parse(commissionedAtString) : null;
        var conditionNotes = DataTableHelper.getStringOrNull(entry, "conditionNotes");
        var condition = DataTableHelper.getStringOrNull(entry, "condition");

        return new EquipmentRequest(
                entry.get("serialNumber"),
                uid,
                equipmentTypeSlug,
                statusSlug,
                model,
                commissionedAt,
                conditionNotes,
                condition
        );
    }
}
