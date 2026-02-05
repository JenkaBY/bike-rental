package com.github.jenkaby.bikerental.equipment.web.query.dto;

import java.util.Set;

public record EquipmentStatusResponse(
        String slug,
        String name,
        String description,
        Set<String> allowedTransitions
) {
}
