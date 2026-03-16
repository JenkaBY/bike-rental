package com.github.jenkaby.bikerental.equipment.application.service;

import com.github.jenkaby.bikerental.equipment.application.usecase.GetEquipmentByIdsUseCase;
import com.github.jenkaby.bikerental.equipment.domain.model.Equipment;
import com.github.jenkaby.bikerental.equipment.domain.repository.EquipmentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
class GetEquipmentByIdsService implements GetEquipmentByIdsUseCase {

    private final EquipmentRepository repository;

    GetEquipmentByIdsService(EquipmentRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Equipment> execute(List<Long> ids) {
        return repository.findByIds(ids);
    }
}
