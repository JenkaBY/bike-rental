package com.github.jenkaby.bikerental.componenttest.transformer;

import com.github.jenkaby.bikerental.componenttest.transformer.shared.Aliases;
import com.github.jenkaby.bikerental.shared.domain.event.RentalCreated;
import io.cucumber.java.DataTableType;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public class RentalCreatedEventTransformer {

    @DataTableType
    public RentalCreated transform(Map<String, String> entry) {
        Long rentalId = DataTableHelper.toLong(entry, "rentalId");
        var equipmentIds = DataTableHelper.toLongList(entry, "eqIds");

        UUID customerId = null;
        String customerIdString = DataTableHelper.getStringOrNull(entry, "customerId");
        if (customerIdString != null && !customerIdString.isBlank()) {
            customerId = Aliases.getCustomerId(customerIdString);
        }

        String status = DataTableHelper.getStringOrNull(entry, "status");
        Instant createdAt = DataTableHelper.toInstant(entry, "createdAt");

        return new RentalCreated(rentalId, customerId, equipmentIds, status, createdAt);
    }
}
