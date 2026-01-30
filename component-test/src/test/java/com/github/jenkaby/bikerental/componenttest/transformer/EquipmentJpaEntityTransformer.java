package com.github.jenkaby.bikerental.componenttest.transformer;

import com.github.jenkaby.bikerental.equipment.infrastructure.persistence.entity.EquipmentJpaEntity;
import io.cucumber.java.DataTableType;

import java.util.Map;

public class EquipmentJpaEntityTransformer {

    @DataTableType
    public EquipmentJpaEntity equipmentJpaEntity(Map<String, String> entry) {
        EquipmentJpaEntity entity = new EquipmentJpaEntity();
        entity.setSerialNumber(entry.get("serialNumber"));
        entity.setUid(entry.get("uid"));
        entity.setStatusSlug(entry.get("status"));
        entity.setEquipmentTypeSlug(entry.get("type"));
        entity.setModel(entry.getOrDefault("model", null));
        String commissionedAtStr = entry.get("commissionedAt");
        if (commissionedAtStr != null && !commissionedAtStr.isEmpty()) {
            entity.setCommissionedAt(java.time.LocalDate.parse(commissionedAtStr));
        } else {
            entity.setCommissionedAt(null);
        }
        entity.setCondition(entry.get("condition"));
        return entity;
    }
}
