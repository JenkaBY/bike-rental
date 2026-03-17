package com.github.jenkaby.bikerental.componenttest.transformer;

import com.github.jenkaby.bikerental.componenttest.transformer.shared.Aliases;
import com.github.jenkaby.bikerental.rental.web.query.dto.RentalSummaryResponse;
import io.cucumber.java.DataTableType;

import java.util.Map;
import java.util.Optional;

public class RentalSummaryResponseTransformer {

    @DataTableType
    public RentalSummaryResponse rentalSummaryResponse(Map<String, String> entry) {
        var idString = DataTableHelper.getStringOrNull(entry, "id");
        var id = Optional.ofNullable(idString)
                .map(Long::valueOf)
                .orElse(null);

        var customerIdString = DataTableHelper.getStringOrNull(entry, "customerId");
        var customerId = Optional.ofNullable(customerIdString)
                .map(Aliases::getCustomerId)
                .orElse(null);

        var equipmentIds = DataTableHelper.toLongList(entry, "equipmentIds");
        var status = DataTableHelper.getStringOrNull(entry, "status");

        var startedAt = DataTableHelper.toLocalDateTime(entry, "startedAt");
        var expectedReturnAt = DataTableHelper.toLocalDateTime(entry, "expectedReturnAt");

        var overdueMinutesString = DataTableHelper.getStringOrNull(entry, "overdueMin");
        var overdueMinutes = Optional.ofNullable(overdueMinutesString)
                .map(Integer::valueOf)
                .orElse(0);

        return new RentalSummaryResponse(
                id,
                customerId,
                equipmentIds,
                status,
                startedAt,
                expectedReturnAt,
                overdueMinutes
        );
    }
}
