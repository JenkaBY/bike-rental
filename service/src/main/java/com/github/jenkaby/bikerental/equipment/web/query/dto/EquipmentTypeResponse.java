package com.github.jenkaby.bikerental.equipment.web.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Equipment type")
public record EquipmentTypeResponse(
        @Schema(description = "Slug identifier", example = "bike") @NotNull String slug,
        @Schema(description = "Display name", example = "Bicycle") @NotNull String name,
        @Schema(description = "Description") String description
) {
}
