package com.github.jenkaby.bikerental.equipment.web.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Equipment type")
public record EquipmentTypeResponse(
        @Schema(description = "Slug identifier", example = "bike") String slug,
        @Schema(description = "Display name", example = "Bicycle") String name,
        @Schema(description = "Description") String description
) {
}
