package com.github.jenkaby.bikerental.equipment.web.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.Nullable;

import java.util.Set;

@Schema(description = "Equipment status with allowed transitions")
public record EquipmentStatusResponse(
        @Schema(description = "Slug identifier", example = "available") @NotNull String slug,
        @Schema(description = "Display name", example = "Available") @NotNull String name,
        @Schema(description = "Description") @Nullable String description,
        @Schema(description = "Status slugs this status can transition to") @NotNull Set<String> allowedTransitions
) {
}
