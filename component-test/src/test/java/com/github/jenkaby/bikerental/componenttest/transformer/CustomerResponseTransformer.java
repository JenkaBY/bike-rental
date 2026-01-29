package com.github.jenkaby.bikerental.componenttest.transformer;

import com.github.jenkaby.bikerental.componenttest.transformer.shared.Aliases;
import com.github.jenkaby.bikerental.customer.web.query.dto.CustomerResponse;
import io.cucumber.java.DataTableType;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

public class CustomerResponseTransformer {

    @DataTableType
    public CustomerResponse customerResponse(Map<String, String> entry) {
        var idString = DataTableHelper.getStringOrNull(entry, "id");
        var id = Optional.ofNullable(idString)
                .map(Aliases::getCustomerId)
                .orElse(null);

        var birthDateString = DataTableHelper.getStringOrNull(entry, "birthDate");
        var birthDate = birthDateString != null ? LocalDate.parse(birthDateString) : null;

        var email = DataTableHelper.getStringOrNull(entry, "email");
        var comments = DataTableHelper.getStringOrNull(entry, "comments");

        return new CustomerResponse(
                id,
                entry.get("phone"),
                entry.get("firstName"),
                entry.get("lastName"),
                email,
                birthDate,
                comments
        );
    }
}
