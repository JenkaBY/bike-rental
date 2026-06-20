package com.github.jenkaby.bikerental.rental.web.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Equipment item within a rental summary")
public record RentalSummaryEquipmentResponse(
        @Schema(description = "Equipment ID", example = "1") @NotNull Long equipmentId,
        @Schema(description = "Equipment UID", example = "BIKE-001") String equipmentUid,
        @Schema(description = "Rental equipment status", example = "ACTIVE") @NotNull String status
) {
}
