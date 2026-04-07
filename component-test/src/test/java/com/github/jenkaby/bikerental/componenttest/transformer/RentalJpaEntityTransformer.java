package com.github.jenkaby.bikerental.componenttest.transformer;

import com.github.jenkaby.bikerental.componenttest.transformer.shared.Aliases;
import com.github.jenkaby.bikerental.rental.domain.model.RentalStatus;
import com.github.jenkaby.bikerental.rental.infrastructure.persistence.entity.RentalJpaEntity;
import io.cucumber.java.DataTableType;

import java.util.Map;
import java.util.Optional;

public class RentalJpaEntityTransformer {

    @DataTableType
    public RentalJpaEntity transform(Map<String, String> entry) {
        var id = DataTableHelper.toLong(entry, "id");
        var customerIdString = DataTableHelper.getStringOrNull(entry, "customerId");
        var customerId = Optional.ofNullable(customerIdString)
                .map(Aliases::getCustomerId)
                .orElse(null);

        var statusString = DataTableHelper.getStringOrNull(entry, "status");
        var status = statusString != null ? RentalStatus.valueOf(statusString) : null;
        var startedAt = DataTableHelper.toLocalDateTime(entry, "startedAt");
        var expectedReturnAt = DataTableHelper.toLocalDateTime(entry, "expectedReturnAt");
        var actualReturnAt = DataTableHelper.toLocalDateTime(entry, "actualReturnAt");
        var plannedDurationMinutes = DataTableHelper.toInt(entry, "plannedDuration");
        var actualDurationMinutes = DataTableHelper.toInt(entry, "actualDuration");

        var createdAt = DataTableHelper.parseLocalDateTimeToInstant(entry, "createdAt");
        var updatedAt = DataTableHelper.parseLocalDateTimeToInstant(entry, "updatedAt");


        return RentalJpaEntity.builder()
                .id(id)
                .customerId(customerId)
                .status(status)
                .startedAt(startedAt)
                .expectedReturnAt(expectedReturnAt)
                .actualReturnAt(actualReturnAt)
                .plannedDurationMinutes(plannedDurationMinutes)
                .actualDurationMinutes(actualDurationMinutes)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }
}
