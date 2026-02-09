package com.github.jenkaby.bikerental.componenttest.transformer;

import com.github.jenkaby.bikerental.componenttest.transformer.shared.Aliases;
import com.github.jenkaby.bikerental.rental.web.command.dto.CreateRentalRequest;
import io.cucumber.java.DataTableType;

import java.util.Map;

public class RentalRequestTransformer {

    @DataTableType
    public CreateRentalRequest createRentalRequest(Map<String, String> entry) {
        var customerId = Aliases.getCustomerId(entry.get("customerId"));
        var equipmentId = DataTableHelper.toLong(entry, "equipmentId");
        var duration = DataTableHelper.toDuration(entry, "duration");
        var startTime = DataTableHelper.toLocalDateTime(entry, "startTime");
        var tariffId = DataTableHelper.toLong(entry, "tariffId");

        return new CreateRentalRequest(
                customerId,
                equipmentId,
                duration,
                startTime,
                tariffId
        );
    }
}
