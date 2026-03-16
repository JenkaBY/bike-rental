package com.github.jenkaby.bikerental.rental.web.command.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Schema(description = "Request body for creating a rental via Fast Path")
public record CreateRentalRequest(
        @Schema(description = "Customer UUID") @NotNull(message = "Customer ID is required") UUID customerId,
        @Schema(description = "List of Equipment IDs to rent (preferred)") List<@Positive Long> equipmentIds,
        @Schema(description = "Planned rental duration (ISO-8601)", example = "PT2H") @NotNull(message = "Duration is required") Duration duration,
        @Schema(description = "Optional tariff ID; auto-selected if not provided", example = "5") Long tariffId
) {
}
