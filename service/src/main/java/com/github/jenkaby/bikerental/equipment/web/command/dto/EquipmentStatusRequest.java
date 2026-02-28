package com.github.jenkaby.bikerental.equipment.web.command.dto;

import com.github.jenkaby.bikerental.shared.web.support.Slug;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.HashSet;
import java.util.Set;

@Schema(description = "Request body for creating or updating an equipment status")
public record EquipmentStatusRequest(
        @Schema(description = "URL-friendly identifier", example = "available") @Slug String slug,
        @Schema(description = "Display name", example = "Available") String name,
        @Schema(description = "Description") String description,
        @Schema(description = "Set of status slugs this status can transition to", example = "[\"rented\", \"maintenance\"]")
        Set<@Slug String> allowedTransitions
) {
        public EquipmentStatusRequest {
                allowedTransitions = allowedTransitions == null ? Set.of() : new HashSet<>(allowedTransitions);
        }

        public EquipmentStatusRequest(String slug, String name, String description) {
                this(slug, name, description, Set.of());
        }
}
