package com.github.jenkaby.bikerental.equipment.domain.repository;

import com.github.jenkaby.bikerental.equipment.domain.model.Equipment;
import com.github.jenkaby.bikerental.equipment.shared.domain.model.vo.SerialNumber;
import com.github.jenkaby.bikerental.equipment.shared.domain.model.vo.Uid;

import java.util.List;
import java.util.Optional;

public interface EquipmentRepository {
    Equipment save(Equipment equipment);

    Optional<Equipment> findById(Long id);

    Optional<Equipment> findBySerialNumber(SerialNumber serialNumber);

    Optional<Equipment> findByUid(Uid uid);

    List<Equipment> findAll(Optional<String> statusSlug, Optional<String> typeSlug);

    boolean existsBySerialNumber(SerialNumber serialNumber);
}
