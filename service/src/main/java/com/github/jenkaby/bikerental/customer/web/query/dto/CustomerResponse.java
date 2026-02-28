package com.github.jenkaby.bikerental.customer.web.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "Full customer profile")
public record CustomerResponse(
        @Schema(description = "Customer UUID") UUID id,
        @Schema(description = "Phone number", example = "+79161234567") String phone,
        @Schema(description = "First name", example = "Ivan") String firstName,
        @Schema(description = "Last name", example = "Ivanov") String lastName,
        @Schema(description = "Email address", example = "ivan@example.com") String email,
        @Schema(description = "Date of birth", example = "1990-05-15") LocalDate birthDate,
        @Schema(description = "Comments") String comments
) {
}
