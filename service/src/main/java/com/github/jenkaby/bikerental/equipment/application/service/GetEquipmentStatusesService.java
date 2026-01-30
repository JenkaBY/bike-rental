package com.github.jenkaby.bikerental.equipment.application.service;

import com.github.jenkaby.bikerental.equipment.application.usecase.GetEquipmentStatusesUseCase;
import com.github.jenkaby.bikerental.equipment.domain.model.EquipmentStatus;
import com.github.jenkaby.bikerental.equipment.domain.repository.EquipmentStatusRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
class GetEquipmentStatusesService implements GetEquipmentStatusesUseCase {

    private final EquipmentStatusRepository repository;

    GetEquipmentStatusesService(EquipmentStatusRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<EquipmentStatus> findAll() {
        return repository.findAll();
    }

    @Override
    public Optional<EquipmentStatus> findBySlug(String slug) {
        return repository.findBySlug(slug);
    }
}
