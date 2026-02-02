package com.github.jenkaby.bikerental.equipment.application.service;

import com.github.jenkaby.bikerental.equipment.application.usecase.GetEquipmentTypesUseCase;
import com.github.jenkaby.bikerental.equipment.domain.model.EquipmentType;
import com.github.jenkaby.bikerental.equipment.domain.repository.EquipmentTypeRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
class GetEquipmentTypesService implements GetEquipmentTypesUseCase {

    private final EquipmentTypeRepository repository;

    GetEquipmentTypesService(EquipmentTypeRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<EquipmentType> findAll() {
        return repository.findAll();
    }

    @Override
    public Optional<EquipmentType> findBySlug(String slug) {
        return repository.findBySlug(slug);
    }
}
