package com.github.jenkaby.bikerental.customer.web.query.dto;

import java.time.LocalDate;
import java.util.UUID;

public record CustomerResponse(
        UUID id,
        String phone,
        String firstName,
        String lastName,
        String email,
        LocalDate birthDate,
        String comments
) {
}
