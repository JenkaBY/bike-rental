package com.github.jenkaby.bikerental.componenttest.transformer;

import com.github.jenkaby.bikerental.shared.domain.event.RentalCompleted;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
import io.cucumber.java.DataTableType;

import java.util.Map;
import java.util.Optional;

public class RentalCompletedEventTransformer {

    @DataTableType
    public RentalCompleted rentalCompletedEvent(Map<String, String> entry) {
        var rentalId = DataTableHelper.toLong(entry, "rentalId");
        var equipmentIds = DataTableHelper.toLongList(entry, "eqIds");
        var returnedEquipmentIds = DataTableHelper.toLongList(entry, "returnedEqIds");
        var returnTime = DataTableHelper.toLocalDateTime(entry, "returnTime");
        var finalCost = Optional.ofNullable(DataTableHelper.toBigDecimal(entry, "totalCost"))
                .map(Money::of)
                .orElse(null);
        return new RentalCompleted(rentalId, equipmentIds, returnedEquipmentIds, returnTime, finalCost);
    }
}

