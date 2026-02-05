package com.github.jenkaby.bikerental.componenttest.transformer;

import com.github.jenkaby.bikerental.equipment.web.command.dto.EquipmentStatusRequest;
import io.cucumber.java.DataTableType;

import java.util.Map;
import java.util.Set;

public class EquipmentStatusRequestTransformer {

    @DataTableType
    public EquipmentStatusRequest equipmentStatus(Map<String, String> entry) {
        var description = DataTableHelper.getStringOrNull(entry, "description");
        var transitions = DataTableHelper.getSetOrDefault(entry, "transitions", Set.of());
        return new EquipmentStatusRequest(
                entry.get("slug"),
                entry.get("name"),
                description,
                transitions
        );
    }
}
