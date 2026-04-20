package com.github.jenkaby.bikerental.customer.web.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(description = "Compact customer info returned in search results")
public record CustomerSearchResponse(
        @Schema(description = "Customer UUID") @NotNull UUID id,
        @Schema(description = "Phone number", example = "+79161234567") @NotNull String phone,
        @Schema(description = "First name", example = "Ivan") String firstName,
        @Schema(description = "Last name", example = "Ivanov") String lastName
) {
}
