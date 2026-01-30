package com.github.jenkaby.bikerental.equipment.application.usecase;

import com.github.jenkaby.bikerental.equipment.domain.model.EquipmentType;

public interface CreateEquipmentTypeUseCase {
    EquipmentType execute(CreateEquipmentTypeCommand command);

    record CreateEquipmentTypeCommand(
            String slug,
            String name,
            String description
    ) {
    }
}
