package com.github.jenkaby.bikerental.componenttest.transformer;

import com.github.jenkaby.bikerental.componenttest.transformer.shared.Aliases;
import com.github.jenkaby.bikerental.customer.infrastructure.persistence.entity.CustomerJpaEntity;
import io.cucumber.java.DataTableType;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;


public class CustomerJpaEntityTransformer {

    @DataTableType
    public CustomerJpaEntity createCustomerTestRequest(Map<String, String> entry) {
        var birthDateString = DataTableHelper.getStringOrNull(entry, "birthDate");
        var birthDate = birthDateString != null ? LocalDate.parse(birthDateString) : null;
        var email = DataTableHelper.getStringOrNull(entry, "email");
        var comments = DataTableHelper.getStringOrNull(entry, "comments");
        return new CustomerJpaEntity(
                Optional.ofNullable(Aliases.getCustomerId(entry.get("id"))).orElse(UUID.randomUUID()),
                entry.get("phone"),
                entry.get("firstName"),
                entry.get("lastName"),
                email,
                birthDate,
                comments,
                Instant.now()
        );
    }
}
