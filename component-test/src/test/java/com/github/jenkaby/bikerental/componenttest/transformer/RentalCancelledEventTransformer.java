package com.github.jenkaby.bikerental.componenttest.transformer;

import com.github.jenkaby.bikerental.componenttest.transformer.shared.Aliases;
import com.github.jenkaby.bikerental.shared.domain.event.RentalCancelled;
import io.cucumber.java.DataTableType;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class RentalCancelledEventTransformer {

    @DataTableType
    public RentalCancelled transform(Map<String, String> entry) {
        var rentalId = Optional.ofNullable(DataTableHelper.getStringOrNull(entry, "rentalId"))
                .map(Long::valueOf)
                .orElse(null);

        var customerIdString = DataTableHelper.getStringOrNull(entry, "customerId");
        var customerId = Optional.ofNullable(customerIdString)
                .map(Aliases::getCustomerId)
                .orElse(null);

        var eqIdsString = DataTableHelper.getStringOrNull(entry, "equipmentIds");
        List<Long> equipmentIds = Optional.ofNullable(eqIdsString)
                .map(s -> Arrays.stream(s.split(","))
                        .map(String::trim)
                        .map(Long::valueOf)
                        .toList())
                .orElse(List.of());

        return new RentalCancelled(rentalId, customerId, equipmentIds);
    }
}