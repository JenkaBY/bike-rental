package com.github.jenkaby.bikerental.componenttest.transformer;

import com.github.jenkaby.bikerental.customer.web.command.dto.CreateCustomerRequest;
import io.cucumber.java.DataTableType;

import java.time.LocalDate;
import java.util.Map;


public class CreateCustomerRequestTransformer {


    @DataTableType
    public CreateCustomerRequest createCustomerTestRequest(Map<String, String> entry) {
        var birthDateString = DataTableHelper.getStringOrNull(entry, "birthDate");
        var birthDate = birthDateString != null ? LocalDate.parse(birthDateString) : null;
        var email = DataTableHelper.getStringOrNull(entry, "email");
        return new CreateCustomerRequest(
                entry.get("phone"),
                entry.get("firstName"),
                entry.get("lastName"),
                email,
                birthDate
        );
    }
}
