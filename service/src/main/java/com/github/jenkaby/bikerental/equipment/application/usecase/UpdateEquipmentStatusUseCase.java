package com.github.jenkaby.bikerental.equipment.application.usecase;

import com.github.jenkaby.bikerental.equipment.domain.model.EquipmentStatus;

public interface UpdateEquipmentStatusUseCase {
    EquipmentStatus execute(UpdateEquipmentStatusCommand command);

    record UpdateEquipmentStatusCommand(
            String slug,
            String name,
            String description
    ) {
    }
}
