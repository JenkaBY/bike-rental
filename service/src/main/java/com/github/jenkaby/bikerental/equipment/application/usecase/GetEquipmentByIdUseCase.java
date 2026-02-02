package com.github.jenkaby.bikerental.equipment.application.usecase;

import com.github.jenkaby.bikerental.equipment.domain.model.Equipment;

import java.util.Optional;

public interface GetEquipmentByIdUseCase {
    Optional<Equipment> execute(Long id);
}
