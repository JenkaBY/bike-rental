package com.github.jenkaby.bikerental.componenttest.transformer;

import com.github.jenkaby.bikerental.equipment.web.query.dto.EquipmentStatusResponse;
import io.cucumber.java.DataTableType;

import java.util.Map;
import java.util.Set;

public class EquipmentStatusResponseTransformer {

    @DataTableType
    public EquipmentStatusResponse equipmentStatus(Map<String, String> entry) {
        var description = DataTableHelper.getStringOrNull(entry, "description");
        var transitions = DataTableHelper.getSetOrDefault(entry, "transitions", Set.of());
        return new EquipmentStatusResponse(
                entry.get("slug"),
                entry.get("name"),
                description,
                transitions
        );
    }
}
