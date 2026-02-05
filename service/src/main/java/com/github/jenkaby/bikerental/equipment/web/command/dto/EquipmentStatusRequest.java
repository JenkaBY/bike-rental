package com.github.jenkaby.bikerental.equipment.web.command.dto;

import com.github.jenkaby.bikerental.shared.web.support.Slug;

import java.util.HashSet;
import java.util.Set;

public record EquipmentStatusRequest(
        @Slug
        String slug,
        String name,
        String description,
        Set<@Slug String> allowedTransitions
) {
        public EquipmentStatusRequest {
                allowedTransitions = allowedTransitions == null ? Set.of() : new HashSet<>(allowedTransitions);
        }

        public EquipmentStatusRequest(String slug, String name, String description) {
                this(slug, name, description, Set.of());
        }
}
