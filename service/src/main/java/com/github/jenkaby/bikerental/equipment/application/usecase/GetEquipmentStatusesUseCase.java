package com.github.jenkaby.bikerental.equipment.application.usecase;

import com.github.jenkaby.bikerental.equipment.domain.model.EquipmentStatus;

import java.util.List;
import java.util.Optional;

public interface GetEquipmentStatusesUseCase {
    List<EquipmentStatus> findAll();

    Optional<EquipmentStatus> findBySlug(String slug);
}
