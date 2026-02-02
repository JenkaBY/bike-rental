package com.github.jenkaby.bikerental.equipment.application.usecase;

import com.github.jenkaby.bikerental.equipment.domain.model.EquipmentType;

public interface UpdateEquipmentTypeUseCase {
    EquipmentType execute(UpdateEquipmentTypeCommand command);

    record UpdateEquipmentTypeCommand(
            String slug,
            String name,
            String description
    ) {
    }
}
