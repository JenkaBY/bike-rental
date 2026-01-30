package com.github.jenkaby.bikerental.equipment.application.usecase;

import com.github.jenkaby.bikerental.equipment.domain.model.EquipmentType;

import java.util.List;
import java.util.Optional;

public interface GetEquipmentTypesUseCase {
    List<EquipmentType> findAll();

    Optional<EquipmentType> findBySlug(String slug);
}
