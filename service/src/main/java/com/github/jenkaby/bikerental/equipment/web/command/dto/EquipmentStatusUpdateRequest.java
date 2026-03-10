package com.github.jenkaby.bikerental.equipment.web.command.dto;

import com.github.jenkaby.bikerental.shared.web.support.Slug;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;

import java.util.HashSet;
import java.util.Set;

@Schema(description = "Request body for creating or updating an equipment status")
public record EquipmentStatusUpdateRequest(
        @Schema(description = "Display name", example = "Available") @NotEmpty String name,
        @Schema(description = "Description") String description,
        @Schema(description = "Set of status slugs this status can transition to", example = "[\"rented\", \"maintenance\"]")
        Set<@Slug String> allowedTransitions
) {
    public EquipmentStatusUpdateRequest {
        allowedTransitions = allowedTransitions == null ? Set.of() : new HashSet<>(allowedTransitions);
    }

    public EquipmentStatusUpdateRequest(String name, String description) {
        this(name, description, Set.of());
    }
}
