package com.github.jenkaby.bikerental.customer.web.query.dto;

import java.util.UUID;

public record CustomerSearchResponse(
        UUID id,
        String phone,
        String firstName,
        String lastName
) {
}
