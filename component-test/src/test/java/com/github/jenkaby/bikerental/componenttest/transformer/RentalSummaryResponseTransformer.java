package com.github.jenkaby.bikerental.componenttest.transformer;

import com.github.jenkaby.bikerental.componenttest.transformer.shared.Aliases;
import com.github.jenkaby.bikerental.rental.web.query.dto.RentalSummaryEquipmentResponse;
import com.github.jenkaby.bikerental.rental.web.query.dto.RentalSummaryResponse;
import io.cucumber.java.DataTableType;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;

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

        var equipments = toEquipments(entry);
        var status = DataTableHelper.getStringOrNull(entry, "status");

        var startedAt = DataTableHelper.parseLocalDateTimeToInstant(entry, "startedAt");
        var expectedReturnAt = DataTableHelper.parseLocalDateTimeToInstant(entry, "expectedReturnAt");

        var overdueMinutesString = DataTableHelper.getStringOrNull(entry, "overdueMin");
        var overdueMinutes = Optional.ofNullable(overdueMinutesString)
                .map(Integer::valueOf)
                .orElse(null);
        var plannedDuration = DataTableHelper.toInt(entry, "plannedDuration");
        var actualDuration = DataTableHelper.toInt(entry, "actualDuration");

        var estimatedCost = DataTableHelper.toBigDecimal(entry, "estimatedCost");
        var finalCost = DataTableHelper.toBigDecimal(entry, "finalCost");

        return new RentalSummaryResponse(
                id,
                customerId,
                equipments,
                status,
                startedAt,
                expectedReturnAt,
                overdueMinutes,
                plannedDuration,
                actualDuration,
                estimatedCost,
                finalCost,
                null
        );
    }

    private static List<RentalSummaryEquipmentResponse> toEquipments(Map<String, String> entry) {
        var equipmentIds = DataTableHelper.toLongList(entry, "equipmentIds");
        if (equipmentIds == null) {
            return null;
        }
        var equipmentUids = DataTableHelper.toStringList(entry, "equipmentUids");
        var equipmentStatuses = DataTableHelper.toStringList(entry, "equipmentStatuses");
        return IntStream.range(0, equipmentIds.size())
                .mapToObj(index -> new RentalSummaryEquipmentResponse(
                        equipmentIds.get(index),
                        equipmentUids != null ? equipmentUids.get(index) : null,
                        equipmentStatuses != null ? equipmentStatuses.get(index) : null
                ))
                .toList();
    }
}
