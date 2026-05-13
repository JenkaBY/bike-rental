package com.github.jenkaby.bikerental.componenttest.transformer;

import com.github.jenkaby.bikerental.componenttest.transformer.shared.Aliases;
import com.github.jenkaby.bikerental.rental.web.command.dto.RentalRequest;
import io.cucumber.java.DataTableType;

import java.util.Map;

public class RentalRequestTransformer {

    @DataTableType
    public RentalRequest transform(Map<String, String> entry) {
        var customerId = Aliases.getCustomerId(entry.get("customerId"));
        var equipmentIds = DataTableHelper.toLongList(entry, "equipmentIds");
        var duration = DataTableHelper.toInt(entry, "duration");
        var operator = Aliases.getValueOrDefault(entry.get("operatorId"));

        var specialTariffId = DataTableHelper.toLong(entry, "specialTariffId");
        var specialPrice = DataTableHelper.toBigDecimal(entry, "specialPrice");
        var discountPercent = DataTableHelper.toInt(entry, "discountPercent");

        return new RentalRequest(
                customerId,
                equipmentIds,
                duration,
                operator,
                specialTariffId,
                specialPrice,
                discountPercent
        );
    }
}
