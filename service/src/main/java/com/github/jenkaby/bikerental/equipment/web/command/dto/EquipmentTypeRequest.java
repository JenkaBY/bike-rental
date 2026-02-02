package com.github.jenkaby.bikerental.equipment.web.command.dto;

import com.github.jenkaby.bikerental.shared.web.support.Slug;

public record EquipmentTypeRequest(
        @Slug
        String slug,
        String name,
        String description
) {
}
