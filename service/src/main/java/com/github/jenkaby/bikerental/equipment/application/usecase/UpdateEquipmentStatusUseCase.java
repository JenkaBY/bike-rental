package com.github.jenkaby.bikerental.equipment.application.usecase;

import com.github.jenkaby.bikerental.equipment.domain.model.EquipmentStatus;

import java.util.Set;

public interface UpdateEquipmentStatusUseCase {
    EquipmentStatus execute(UpdateEquipmentStatusCommand command);

    record UpdateEquipmentStatusCommand(
            String slug,
            String name,
            String description,
            Set<String> allowedTransitions
    ) {
    }
}
