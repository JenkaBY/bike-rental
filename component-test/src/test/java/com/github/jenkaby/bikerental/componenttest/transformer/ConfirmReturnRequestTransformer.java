package com.github.jenkaby.bikerental.componenttest.transformer;

import com.github.jenkaby.bikerental.componenttest.transformer.shared.Aliases;
import com.github.jenkaby.bikerental.rental.web.command.dto.ConfirmReturnRequest;
import io.cucumber.java.DataTableType;

import java.util.Map;

public class ConfirmReturnRequestTransformer {

    @DataTableType
    public ConfirmReturnRequest transform(Map<String, String> entry) {
        var operatorId = Aliases.getOperatorId(DataTableHelper.getStringOrNull(entry, "operatorId"));

        return new ConfirmReturnRequest(null, operatorId);
    }
}
