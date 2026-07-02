package com.github.jenkaby.bikerental.componenttest.transformer;

import com.github.jenkaby.bikerental.componenttest.transformer.shared.Aliases;
import com.github.jenkaby.bikerental.rental.web.command.dto.AddRentalEquipmentRequest;
import io.cucumber.java.DataTableType;

import java.util.Map;

public class AddRentalEquipmentRequestTransformer {

    @DataTableType
    public AddRentalEquipmentRequest addRentalEquipmentRequest(Map<String, String> entry) {
        var equipmentIds = DataTableHelper.toLongList(entry, "equipmentIds");
        var operatorId = Aliases.getOperatorId(DataTableHelper.getStringOrNull(entry, "operatorId"));

        return new AddRentalEquipmentRequest(equipmentIds, operatorId);
    }
}
