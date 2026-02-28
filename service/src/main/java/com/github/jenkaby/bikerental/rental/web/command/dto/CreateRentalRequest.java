package com.github.jenkaby.bikerental.rental.web.command.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.Duration;
import java.util.UUID;

@Schema(description = "Request body for creating a rental via Fast Path")
public record CreateRentalRequest(
        @Schema(description = "Customer UUID") @NotNull(message = "Customer ID is required") UUID customerId,
        @Schema(description = "Equipment ID", example = "1") @NotNull(message = "Equipment ID is required") Long equipmentId,
        @Schema(description = "Planned rental duration (ISO-8601)", example = "PT2H") @NotNull(message = "Duration is required") Duration duration,
        @Schema(description = "Optional tariff ID; auto-selected if not provided", example = "5") Long tariffId
) {
}
