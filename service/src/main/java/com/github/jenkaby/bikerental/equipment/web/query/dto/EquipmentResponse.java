package com.github.jenkaby.bikerental.equipment.web.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Equipment record")
public record EquipmentResponse(
        @Schema(description = "Internal ID", example = "1") Long id,
        @Schema(description = "Serial number", example = "SN-123456") String serialNumber,
        @Schema(description = "UID tag", example = "BIKE-001") String uid,
        @Schema(description = "Equipment type slug", example = "bike") String type,
        @Schema(description = "Equipment status slug", example = "available") String status,
        @Schema(description = "Model name", example = "Trek Marlin 5") String model,
        @Schema(description = "Commissioned date", example = "2023-06-01") LocalDate commissionedAt,
        @Schema(description = "Condition", example = "Good") String condition
) {
}
