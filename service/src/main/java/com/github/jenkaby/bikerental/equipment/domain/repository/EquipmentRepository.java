package com.github.jenkaby.bikerental.equipment.domain.repository;

import com.github.jenkaby.bikerental.equipment.domain.model.Equipment;
import com.github.jenkaby.bikerental.equipment.shared.domain.model.vo.SerialNumber;
import com.github.jenkaby.bikerental.equipment.shared.domain.model.vo.Uid;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Page;
import com.github.jenkaby.bikerental.shared.domain.model.vo.PageRequest;

import java.util.Optional;

public interface EquipmentRepository {
    Equipment save(Equipment equipment);

    Optional<Equipment> findById(Long id);

    Page<Equipment> findAll(Optional<String> statusSlug, Optional<String> typeSlug, PageRequest pageRequest);

    boolean existsBySerialNumber(SerialNumber serialNumber);

    boolean existsByUid(Uid uid);
}
