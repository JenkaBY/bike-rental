package com.github.jenkaby.bikerental.componenttest.transformer;

import com.github.jenkaby.bikerental.componenttest.transformer.shared.Aliases;
import com.github.jenkaby.bikerental.rental.web.query.dto.RentalResponse;
import io.cucumber.java.DataTableType;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Optional;

public class RentalResponseTransformer {

    @DataTableType
    public RentalResponse rentalResponse(Map<String, String> entry) {
        var idString = DataTableHelper.getStringOrNull(entry, "id");
        var id = Optional.ofNullable(idString)
                .map(Long::valueOf)
                .orElse(null);

        var customerIdString = DataTableHelper.getStringOrNull(entry, "customerId");
        var customerId = Optional.ofNullable(customerIdString).map(Aliases::getCustomerId)
                .orElse(null);

        var equipmentId = DataTableHelper.toLong(entry, "equipmentId");
        var tariffId = DataTableHelper.toLong(entry, "tariffId");
        var status = DataTableHelper.getStringOrNull(entry, "status");

        var plannedDurationMinutes = DataTableHelper.toInt(entry, "plannedDuration");
        var actualDurationMinutes = DataTableHelper.toInt(entry, "actualDuration");

        LocalDateTime startedAt;
        if ("now()".equals(entry.get("startedAt"))) {
            startedAt = LocalDateTime.ofInstant(Instant.now(), ZoneOffset.systemDefault());
        } else {
            startedAt = DataTableHelper.toLocalDateTime(entry, "startedAt");
        }

        LocalDateTime expectedReturnAt;
        if (startedAt != null && plannedDurationMinutes != null) {
            expectedReturnAt = startedAt.plusMinutes(plannedDurationMinutes);
        } else {
            expectedReturnAt = DataTableHelper.toLocalDateTime(entry, "expectedReturnAt");
        }

        var actualReturnAt = DataTableHelper.toLocalDateTime(entry, "actualReturnAt");

        var estimatedCost = DataTableHelper.toBigDecimal(entry, "estimatedCost");
        var finalCost = DataTableHelper.toBigDecimal(entry, "finalCost");

        return new RentalResponse(
                id,
                customerId,
                equipmentId,
                tariffId,
                status,
                startedAt,
                expectedReturnAt,
                actualReturnAt,
                plannedDurationMinutes,
                actualDurationMinutes,
                estimatedCost,
                finalCost);
    }
}
