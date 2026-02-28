package com.github.jenkaby.bikerental.equipment.web.command.dto;

import com.github.jenkaby.bikerental.shared.web.support.Slug;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request body for creating or updating an equipment type")
public record EquipmentTypeRequest(
        @Schema(description = "URL-friendly identifier", example = "bike") @Slug String slug,
        @Schema(description = "Display name", example = "Bicycle") String name,
        @Schema(description = "Description") String description
) {
}
