package com.github.jenkaby.bikerental.equipment.domain.repository;

import com.github.jenkaby.bikerental.equipment.domain.model.EquipmentType;

import java.util.List;
import java.util.Optional;

public interface EquipmentTypeRepository {
    EquipmentType save(EquipmentType equipmentType);

    Optional<EquipmentType> findBySlug(String slug);

    List<EquipmentType> findAll();

    boolean existsBySlug(String slug);
}
