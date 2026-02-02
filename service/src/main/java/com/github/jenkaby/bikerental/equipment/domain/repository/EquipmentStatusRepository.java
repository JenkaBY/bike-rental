package com.github.jenkaby.bikerental.equipment.domain.repository;

import com.github.jenkaby.bikerental.equipment.domain.model.EquipmentStatus;

import java.util.List;
import java.util.Optional;

public interface EquipmentStatusRepository {
    EquipmentStatus save(EquipmentStatus equipmentStatus);

    Optional<EquipmentStatus> findBySlug(String slug);

    List<EquipmentStatus> findAll();

    boolean existsBySlug(String slug);
}
