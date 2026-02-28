package com.github.jenkaby.bikerental.equipment.web.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;

@Schema(description = "Equipment status with allowed transitions")
public record EquipmentStatusResponse(
        @Schema(description = "Slug identifier", example = "available") String slug,
        @Schema(description = "Display name", example = "Available") String name,
        @Schema(description = "Description") String description,
        @Schema(description = "Status slugs this status can transition to") Set<String> allowedTransitions
) {
}
