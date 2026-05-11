package com.github.jenkaby.bikerental.componenttest.transformer;

import com.github.jenkaby.bikerental.rental.web.query.dto.AvailableEquipmentResponse;
import io.cucumber.java.DataTableType;

import java.util.Map;

public class AvailableEquipmentResponseTransformer {

    @DataTableType
    public AvailableEquipmentResponse transform(Map<String, String> entry) {
        return new AvailableEquipmentResponse(
                DataTableHelper.toLong(entry, "id"),
                DataTableHelper.getStringOrNull(entry, "uid"),
                DataTableHelper.getStringOrNull(entry, "serialNumber"),
                DataTableHelper.getStringOrNull(entry, "type"),
                DataTableHelper.getStringOrNull(entry, "model")
        );
    }
}