package com.github.jenkaby.bikerental.equipment.application.usecase;

import com.github.jenkaby.bikerental.equipment.domain.model.EquipmentStatus;

import java.util.Set;

public interface CreateEquipmentStatusUseCase {
    EquipmentStatus execute(CreateEquipmentStatusCommand command);

    record CreateEquipmentStatusCommand(
            String slug,
            String name,
            String description,
            Set<String> allowedTransitions
    ) {
    }
}
