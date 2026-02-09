package com.github.jenkaby.bikerental.componenttest.transformer;

import com.github.jenkaby.bikerental.equipment.infrastructure.persistence.entity.EquipmentJpaEntity;
import io.cucumber.java.DataTableType;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

public class EquipmentJpaEntityTransformer {

    @DataTableType
    public EquipmentJpaEntity equipmentJpaEntity(Map<String, String> entry) {
        EquipmentJpaEntity entity = new EquipmentJpaEntity();
        entity.setId(Optional.ofNullable(entry.get("id")).map(Long::parseLong).orElse(null));
        entity.setSerialNumber(entry.get("serialNumber"));
        entity.setUid(entry.get("uid"));
        entity.setStatusSlug(entry.get("status"));
        entity.setTypeSlug(entry.get("type"));
        entity.setModel(entry.getOrDefault("model", null));
        String commissionedAtStr = entry.get("commissionedAt");
        if (commissionedAtStr != null && !commissionedAtStr.isEmpty()) {
            entity.setCommissionedAt(java.time.LocalDate.parse(commissionedAtStr));
        } else {
            entity.setCommissionedAt(null);
        }
        entity.setCondition(entry.get("condition"));
        entity.setCreatedAt(Optional.ofNullable(DataTableHelper.toInstant(entry, "createdAt")).orElse(Instant.now()));
        entity.setUpdatedAt(DataTableHelper.toInstant(entry, "updatedAt"));
        return entity;
    }
}
