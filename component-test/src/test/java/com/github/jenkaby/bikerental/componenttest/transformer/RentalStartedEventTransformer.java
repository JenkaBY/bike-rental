package com.github.jenkaby.bikerental.componenttest.transformer;

import com.github.jenkaby.bikerental.componenttest.transformer.shared.Aliases;
import com.github.jenkaby.bikerental.shared.domain.event.RentalStarted;
import io.cucumber.java.DataTableType;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.UUID;

public class RentalStartedEventTransformer {

    @DataTableType
    public RentalStarted rentalStartedEvent(Map<String, String> entry) {
        Long rentalId = DataTableHelper.toLong(entry, "rentalId");

        UUID customerId = null;
        String customerIdString = DataTableHelper.getStringOrNull(entry, "customerId");
        if (customerIdString != null && !customerIdString.isBlank()) {
            customerId = Aliases.getCustomerId(customerIdString);
        }

        Long equipmentId = DataTableHelper.toLong(entry, "equipmentId");

        LocalDateTime startedAt = null;
        String startedAtString = DataTableHelper.getStringOrNull(entry, "startedAt");
        if (startedAtString != null && !startedAtString.isBlank()) {
            if ("now()".equals(startedAtString)) {
                startedAt = LocalDateTime.ofInstant(Instant.now(), ZoneOffset.systemDefault());
            } else {
                startedAt = DataTableHelper.toLocalDateTime(entry, "startedAt");
            }
        }

        LocalDateTime expectedReturnAt = DataTableHelper.toLocalDateTime(entry, "expectedReturnAt");

        return new RentalStarted(rentalId, customerId, equipmentId, startedAt, expectedReturnAt);
    }
}
