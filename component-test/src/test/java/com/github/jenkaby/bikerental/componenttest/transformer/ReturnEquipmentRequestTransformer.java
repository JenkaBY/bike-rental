package com.github.jenkaby.bikerental.componenttest.transformer;

import com.github.jenkaby.bikerental.finance.PaymentMethod;
import com.github.jenkaby.bikerental.rental.web.command.dto.ReturnEquipmentRequest;
import io.cucumber.java.DataTableType;

import java.util.Map;
import java.util.Optional;

public class ReturnEquipmentRequestTransformer {

    @DataTableType
    public ReturnEquipmentRequest returnEquipmentRequest(Map<String, String> entry) {
        var rentalId = DataTableHelper.toLong(entry, "rentalId");
        var equipmentIds = DataTableHelper.toLongList(entry, "equipmentIds");
        var equipmentUids = DataTableHelper.toStringList(entry, "equipmentUids");
        var paymentMethodStr = DataTableHelper.getStringOrNull(entry, "paymentMethod");
        var paymentMethod = Optional.ofNullable(paymentMethodStr)
                .map(PaymentMethod::valueOf)
                .orElse(null);
        var operatorId = DataTableHelper.getStringOrNull(entry, "operatorId");

        return new ReturnEquipmentRequest(rentalId, equipmentIds, equipmentUids, paymentMethod, operatorId);
    }
}

