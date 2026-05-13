package com.github.jenkaby.bikerental.equipment.web.command.dto;

import com.github.jenkaby.bikerental.shared.web.support.Slug;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@Schema(description = "Request body for creating or updating equipment")
public record EquipmentRequest(

        @Schema(description = "Unique serial number", example = "SN-123456")
        @Size(max = 50, message = "Serial number must not exceed 50 characters")
        String serialNumber,

        @Schema(description = "Unique identifier tag (UID)", example = "BIKE-001")
        @Size(max = 100, message = "UID must not exceed 100 characters")
        @NotBlank
        String uid,

        @Schema(description = "Equipment type slug", example = "bike")
        @Slug
        String typeSlug,

        @Schema(description = "Equipment status slug", example = "available")
        @Slug
        @Deprecated(forRemoval = true)
        String statusSlug,

        @Schema(description = "Model name", example = "Trek Marlin 5")
        @Size(max = 200, message = "Model must not exceed 200 characters")
        String model,

        @Schema(description = "Date when equipment was put into service", example = "2023-06-01")
        LocalDate commissionedAt,

        @Schema(description = "Condition description", example = "Good")
        String condition,

        @Schema(description = "Condition slug", example = "GOOD",
                allowableValues = {"GOOD", "MAINTENANCE", "BROKEN", "DECOMMISSIONED"})
        String conditionSlug
) {
}
