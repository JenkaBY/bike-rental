package com.github.jenkaby.bikerental.componenttest.transformer;

import com.github.jenkaby.bikerental.componenttest.transformer.shared.Aliases;
import com.github.jenkaby.bikerental.rental.web.command.dto.JsonPatchOperation;
import com.github.jenkaby.bikerental.rental.web.command.dto.RentalPatchOperation;
import io.cucumber.java.DataTableType;

import java.util.Map;

public class RentalPatchOperationTransformer {

    @DataTableType
    public RentalPatchOperation rentalPatchOperation(Map<String, String> entry) {
        var op = JsonPatchOperation.fromValue(entry.get("op"));
        var path = entry.get("path");
        Object value;
        if (path.contains("customerId")) {
            value = Aliases.getValue(entry.get("value"));
        } else {
            value = entry.get("value");
        }
        return new RentalPatchOperation(op, path, value);
    }
}
