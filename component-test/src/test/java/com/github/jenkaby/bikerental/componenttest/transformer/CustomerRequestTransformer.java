package com.github.jenkaby.bikerental.componenttest.transformer;

import com.github.jenkaby.bikerental.customer.web.command.dto.CustomerRequest;
import io.cucumber.java.DataTableType;

import java.time.LocalDate;
import java.util.Map;

public class CustomerRequestTransformer {

    @DataTableType
    public CustomerRequest customerRequest(Map<String, String> entry) {
        var birthDateString = DataTableHelper.getStringOrNull(entry, "birthDate");
        var birthDate = birthDateString != null ? LocalDate.parse(birthDateString) : null;
        var email = DataTableHelper.getStringOrNull(entry, "email");
        var comments = DataTableHelper.getStringOrNull(entry, "comments");
        return new CustomerRequest(
                entry.get("phone"),
                entry.get("firstName"),
                entry.get("lastName"),
                email,
                birthDate,
                comments
        );
    }
}
