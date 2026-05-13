package com.github.jenkaby.bikerental.componenttest.transformer;

import com.github.jenkaby.bikerental.componenttest.transformer.shared.Aliases;
import com.github.jenkaby.bikerental.rental.web.command.dto.LifecycleStatus;
import com.github.jenkaby.bikerental.rental.web.command.dto.RentalLifecycleRequest;
import io.cucumber.java.DataTableType;

import java.util.Map;

public class RentalLifecycleRequestTransformer {

    @DataTableType
    public RentalLifecycleRequest transform(Map<String, String> entry) {
        var statusString = DataTableHelper.getStringOrNull(entry, "status");
        var status = statusString != null ? LifecycleStatus.valueOf(statusString) : null;
        var operatorId = Aliases.getOperatorId(DataTableHelper.getStringOrNull(entry, "operatorId"));
        return new RentalLifecycleRequest(status, operatorId);
    }
}