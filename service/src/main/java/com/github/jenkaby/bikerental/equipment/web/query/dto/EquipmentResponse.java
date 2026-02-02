package com.github.jenkaby.bikerental.equipment.web.query.dto;

import java.time.LocalDate;

public record EquipmentResponse(
        Long id,
        String serialNumber,
        String uid,
        String type,
        String status,
        String model,
        LocalDate commissionedAt,
        String condition
) {
}
