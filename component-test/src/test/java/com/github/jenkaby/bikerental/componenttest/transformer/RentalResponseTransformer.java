package com.github.jenkaby.bikerental.componenttest.transformer;

import com.github.jenkaby.bikerental.componenttest.transformer.shared.Aliases;
import com.github.jenkaby.bikerental.rental.web.query.dto.RentalResponse;
import io.cucumber.java.DataTableType;

import java.time.Instant;
import java.util.ArrayList;
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

        var status = DataTableHelper.getStringOrNull(entry, "status");

        var plannedDurationMinutes = DataTableHelper.toInt(entry, "plannedDuration");
        var actualDurationMinutes = DataTableHelper.toInt(entry, "actualDuration");

        Instant startedAt;
        if ("now()".equals(entry.get("startedAt"))) {
            startedAt = Instant.now();
        } else {
            startedAt = DataTableHelper.parseLocalDateTimeToInstant(entry, "startedAt");
        }

        Instant expectedReturnAt;
        if (startedAt != null && plannedDurationMinutes != null) {
            expectedReturnAt = startedAt.plusSeconds(plannedDurationMinutes * 60L);
        } else {
            expectedReturnAt = DataTableHelper.parseLocalDateTimeToInstant(entry, "expectedReturnAt");
        }

        var actualReturnAt = DataTableHelper.parseLocalDateTimeToInstant(entry, "actualReturnAt");

        var estimatedCost = DataTableHelper.toBigDecimal(entry, "estimatedCost");
        var specialPrice = DataTableHelper.toBigDecimal(entry, "specialPrice");
        var discountPercent = DataTableHelper.toInt(entry, "discountPercent");
        var finalCost = DataTableHelper.toBigDecimal(entry, "totalCost");
        var version = DataTableHelper.toLong(entry, "version");
        if (DataTableHelper.toBigDecimal(entry, "finalCost") != null) {
            throw new IllegalArgumentException("finalCost must NOT be provided. It must be total cost for RentalResponse");
        }
        return new RentalResponse(
                id,
                version,
                customerId,
                new ArrayList<>(),
                status,
                startedAt,
                expectedReturnAt,
                actualReturnAt,
                plannedDurationMinutes,
                actualDurationMinutes,
                estimatedCost,
                specialPrice,
                discountPercent,
                finalCost);
    }
}
