package com.github.jenkaby.bikerental.componenttest.transformer;

import com.github.jenkaby.bikerental.componenttest.transformer.shared.Aliases;
import com.github.jenkaby.bikerental.rental.infrastructure.persistence.entity.RentalJpaEntity;
import io.cucumber.java.DataTableType;

import java.util.Map;
import java.util.Optional;

public class RentalJpaEntityTransformer {

    @DataTableType
    public RentalJpaEntity rentalJpaEntity(Map<String, String> entry) {
        var id = DataTableHelper.toLong(entry, "id");
        var customerIdString = DataTableHelper.getStringOrNull(entry, "customerId");
        var customerId = Optional.ofNullable(customerIdString)
                .map(Aliases::getCustomerId)
                .orElse(null);
        var equipmentId = DataTableHelper.toLong(entry, "equipmentId");
        var tariffId = DataTableHelper.toLong(entry, "tariffId");
        var status = DataTableHelper.getStringOrNull(entry, "status");
        var startedAt = DataTableHelper.toLocalDateTime(entry, "startedAt");
        var expectedReturnAt = DataTableHelper.toLocalDateTime(entry, "expectedReturnAt");
        var actualReturnAt = DataTableHelper.toLocalDateTime(entry, "actualReturnAt");
        var plannedDurationMinutes = DataTableHelper.toInt(entry, "plannedDuration");
        var actualDurationMinutes = DataTableHelper.toInt(entry, "actualDuration");
        var estimatedCost = DataTableHelper.toBigDecimal(entry, "estimatedCost");
        var finalCost = DataTableHelper.toBigDecimal(entry, "finalCost");

        var createdAt = DataTableHelper.parseLocalDateTimeToInstant(entry, "createdAt");
        var updatedAt = DataTableHelper.parseLocalDateTimeToInstant(entry, "updatedAt");
        ;

        return RentalJpaEntity.builder()
                .id(id)
                .customerId(customerId)
                .equipmentId(equipmentId)
                .tariffId(tariffId)
                .status(status)
                .startedAt(startedAt)
                .expectedReturnAt(expectedReturnAt)
                .actualReturnAt(actualReturnAt)
                .plannedDurationMinutes(plannedDurationMinutes)
                .actualDurationMinutes(actualDurationMinutes)
                .estimatedCost(estimatedCost)
                .finalCost(finalCost)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }
}
