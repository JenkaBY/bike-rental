package com.github.jenkaby.bikerental.componenttest.transformer;

import com.github.jenkaby.bikerental.equipment.web.query.dto.EquipmentStatusResponse;
import io.cucumber.java.DataTableType;

import java.util.Map;

public class VocabularyUnitResponseTransformer {

    @DataTableType
    public EquipmentStatusResponse equipmentStatus(Map<String, String> entry) {
        var description = DataTableHelper.getStringOrNull(entry, "description");
        return new EquipmentStatusResponse(
                entry.get("slug"),
                entry.get("name"),
                description
        );
    }
}
