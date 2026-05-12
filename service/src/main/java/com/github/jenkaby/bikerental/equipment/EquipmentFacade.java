package com.github.jenkaby.bikerental.equipment;

import com.github.jenkaby.bikerental.shared.domain.model.Condition;

import java.util.List;
import java.util.Optional;
import java.util.Set;


public interface EquipmentFacade {

    Optional<EquipmentInfo> findById(Long equipmentId);

    List<EquipmentInfo> findByIds(List<Long> equipmentIds);

    List<EquipmentInfo> getEquipmentsByConditions(Set<Condition> conditions, EquipmentSearchFilter filter);
}
