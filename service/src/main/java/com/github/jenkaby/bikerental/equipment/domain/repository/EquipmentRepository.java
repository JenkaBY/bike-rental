package com.github.jenkaby.bikerental.equipment.domain.repository;

import com.github.jenkaby.bikerental.equipment.domain.model.Equipment;
import com.github.jenkaby.bikerental.equipment.shared.domain.model.vo.SerialNumber;
import com.github.jenkaby.bikerental.equipment.shared.domain.model.vo.Uid;
import com.github.jenkaby.bikerental.shared.domain.model.Condition;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Page;
import com.github.jenkaby.bikerental.shared.domain.model.vo.PageRequest;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface EquipmentRepository {
    Equipment save(Equipment equipment);

    Optional<Equipment> findById(Long id);

    List<Equipment> findByIds(Collection<Long> ids);

    Page<Equipment> findAll(String statusSlug, String typeSlug, String searchText, PageRequest pageRequest);

    boolean existsBySerialNumber(SerialNumber serialNumber);

    boolean existsByUid(Uid uid);

    Optional<Equipment> findBySerialNumber(SerialNumber serialNumber);

    Optional<Equipment> findByUid(Uid uid);

    List<Equipment> findByConditions(Set<Condition> conditions, String searchText);
}
