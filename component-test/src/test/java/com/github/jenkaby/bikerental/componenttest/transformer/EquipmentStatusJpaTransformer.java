package com.github.jenkaby.bikerental.componenttest.transformer;

import com.github.jenkaby.bikerental.equipment.infrastructure.persistence.entity.EquipmentStatusJpaEntity;
import io.cucumber.java.DataTableType;

import java.util.Map;
import java.util.Set;

public class EquipmentStatusJpaTransformer {

    @DataTableType
    public EquipmentStatusJpaEntity equipmentStatus(Map<String, String> entry) {
        var description = DataTableHelper.getStringOrNull(entry, "description");
        var transitions = DataTableHelper.getSetOrDefault(entry, "transitions", Set.of());
        return new EquipmentStatusJpaEntity(
                null,
                entry.get("slug"),
                entry.get("name"),
                description,
                transitions
        );
    }
}
