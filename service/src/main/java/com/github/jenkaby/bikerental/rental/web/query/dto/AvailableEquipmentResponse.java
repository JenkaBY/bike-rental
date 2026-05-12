package com.github.jenkaby.bikerental.rental.web.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Equipment available for a new rental")
public record AvailableEquipmentResponse(
        @Schema(description = "Equipment ID", example = "1") Long id,
        @Schema(description = "Equipment UID", example = "BIKE-001") String uid,
        @Schema(description = "Serial number", example = "SN-ABC-001") String serialNumber,
        @Schema(description = "Equipment type slug", example = "mountain-bike") String typeSlug,
        @Schema(description = "Model name", example = "Trek Marlin 5") String model
) {
}