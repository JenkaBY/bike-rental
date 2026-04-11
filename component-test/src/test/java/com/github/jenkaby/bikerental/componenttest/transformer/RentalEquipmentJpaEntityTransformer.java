package com.github.jenkaby.bikerental.componenttest.transformer;

import com.github.jenkaby.bikerental.componenttest.transformer.shared.Aliases;
import com.github.jenkaby.bikerental.rental.domain.model.RentalEquipmentStatus;
import com.github.jenkaby.bikerental.rental.infrastructure.persistence.entity.RentalEquipmentJpaEntity;
import com.github.jenkaby.bikerental.rental.infrastructure.persistence.entity.RentalJpaEntity;
import io.cucumber.java.DataTableType;

import java.util.Map;

public class RentalEquipmentJpaEntityTransformer {

    @DataTableType
    public RentalEquipmentJpaEntity transform(Map<String, String> entry) {
        var id = DataTableHelper.toLong(entry, "id");
        var rentalId = entry.get("rentalId") != null ? Long.valueOf(Aliases.getValueOrDefault(entry.get("rentalId"))) : null;

        var equipmentId = DataTableHelper.toLong(entry, "equipmentId");
        var equipmentUid = DataTableHelper.getStringOrNull(entry, "equipmentUid");
        var tariffId = DataTableHelper.toLong(entry, "tariffId");
        var status = RentalEquipmentStatus.valueOf(DataTableHelper.getStringOrNull(entry, "status"));
        var startedAt = DataTableHelper.toLocalDateTime(entry, "startedAt");
        var expectedReturnAt = DataTableHelper.toLocalDateTime(entry, "expectedReturnAt");
        var actualReturnAt = DataTableHelper.toLocalDateTime(entry, "actualReturnAt");
        var estimatedCost = DataTableHelper.toBigDecimal(entry, "estimatedCost");
        var finalCost = DataTableHelper.toBigDecimal(entry, "totalCost");

        var createdAt = DataTableHelper.parseLocalDateTimeToInstant(entry, "createdAt");
        var updatedAt = DataTableHelper.parseLocalDateTimeToInstant(entry, "updatedAt");

        return RentalEquipmentJpaEntity.builder()
                .id(id)
                .rental(RentalJpaEntity.builder().id(rentalId).build())
                .equipmentId(equipmentId)
                .equipmentUid(equipmentUid)
                .tariffId(tariffId)
                .status(status.name())
                .startedAt(startedAt)
                .expectedReturnAt(expectedReturnAt)
                .actualReturnAt(actualReturnAt)
                .estimatedCost(estimatedCost)
                .finalCost(finalCost)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }
}
