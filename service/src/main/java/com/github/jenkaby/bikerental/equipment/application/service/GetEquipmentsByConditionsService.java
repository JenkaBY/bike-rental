package com.github.jenkaby.bikerental.equipment.application.service;

import com.github.jenkaby.bikerental.equipment.EquipmentSearchFilter;
import com.github.jenkaby.bikerental.equipment.application.usecase.GetEquipmentsByConditionsUseCase;
import com.github.jenkaby.bikerental.equipment.domain.model.Equipment;
import com.github.jenkaby.bikerental.equipment.domain.repository.EquipmentRepository;
import com.github.jenkaby.bikerental.shared.domain.model.Condition;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
class GetEquipmentsByConditionsService implements GetEquipmentsByConditionsUseCase {

    private final EquipmentRepository repository;

    GetEquipmentsByConditionsService(EquipmentRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Equipment> execute(Set<Condition> conditions, EquipmentSearchFilter filter) {
        if (conditions.isEmpty()) {
            throw new IllegalArgumentException("conditions must not be empty");
        }
        return repository.findByConditions(conditions, filter.q());
    }
}
