package com.github.jenkaby.bikerental.componenttest.transformer;

import com.github.jenkaby.bikerental.componenttest.transformer.shared.Aliases;
import com.github.jenkaby.bikerental.rental.web.command.dto.CreateRentalRequest;
import io.cucumber.java.DataTableType;

import java.util.Map;

public class RentalRequestTransformer {

    @DataTableType
    public CreateRentalRequest transform(Map<String, String> entry) {
        var customerId = Aliases.getCustomerId(entry.get("customerId"));
        var equipmentIds = DataTableHelper.toLongList(entry, "equipmentIds");
        var duration = DataTableHelper.toDuration(entry, "duration");
        var tariffId = DataTableHelper.toLong(entry, "tariffId");
        var operator = Aliases.getValueOrDefault(entry.get("operatorId"));

        return new CreateRentalRequest(
                customerId,
                equipmentIds,
                duration,
                tariffId,
                operator
        );
    }
}
