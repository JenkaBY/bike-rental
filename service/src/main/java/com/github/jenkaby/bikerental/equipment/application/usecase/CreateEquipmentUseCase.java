package com.github.jenkaby.bikerental.equipment.application.usecase;

import com.github.jenkaby.bikerental.equipment.domain.model.Equipment;

import java.time.LocalDate;

public interface CreateEquipmentUseCase {
    Equipment execute(CreateEquipmentCommand command);

    record CreateEquipmentCommand(
            String serialNumber,
            String uid,
            String equipmentTypeSlug,
            String statusSlug,
            String model,
            LocalDate commissionedAt,
            String condition
    ) {
    }
}
