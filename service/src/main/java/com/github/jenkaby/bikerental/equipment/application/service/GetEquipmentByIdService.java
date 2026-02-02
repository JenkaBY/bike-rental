package com.github.jenkaby.bikerental.equipment.application.service;

import com.github.jenkaby.bikerental.equipment.application.usecase.GetEquipmentByIdUseCase;
import com.github.jenkaby.bikerental.equipment.domain.model.Equipment;
import com.github.jenkaby.bikerental.equipment.domain.repository.EquipmentRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
class GetEquipmentByIdService implements GetEquipmentByIdUseCase {

    private final EquipmentRepository repository;

    GetEquipmentByIdService(EquipmentRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Equipment> execute(Long id) {
        return repository.findById(id);
    }
}
