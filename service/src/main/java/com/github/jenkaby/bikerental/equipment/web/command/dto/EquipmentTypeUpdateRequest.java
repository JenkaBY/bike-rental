package com.github.jenkaby.bikerental.equipment.web.command.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;

@Schema(description = "Request body for updating an equipment type")
public record EquipmentTypeUpdateRequest(
        @Schema(description = "Display name", example = "Bicycle") @NotEmpty String name,
        @Schema(description = "Description") String description
) {
}
