package com.github.jenkaby.bikerental.equipment.web.query.dto;

import java.time.LocalDate;

public record EquipmentResponse(
        Long id,
        String serialNumber,
        String uid,
        String equipmentTypeSlug,
        String statusSlug,
        String model,
        LocalDate commissionedAt,
        String condition
) {
}
