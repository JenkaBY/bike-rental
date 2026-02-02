package com.github.jenkaby.bikerental.equipment.application.usecase;

import com.github.jenkaby.bikerental.equipment.domain.model.Equipment;

import java.time.LocalDate;

public interface UpdateEquipmentUseCase {
    Equipment execute(UpdateEquipmentCommand command);

    record UpdateEquipmentCommand(
            Long id,
            String serialNumber,
            String uid,
            String typeSlug,
            String statusSlug,
            String model,
            LocalDate commissionedAt,
            String condition
    ) {
    }
}
