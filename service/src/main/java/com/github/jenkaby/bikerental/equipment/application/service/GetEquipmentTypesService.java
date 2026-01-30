package com.github.jenkaby.bikerental.equipment.application.service;

import com.github.jenkaby.bikerental.equipment.application.usecase.GetEquipmentTypesUseCase;
import com.github.jenkaby.bikerental.equipment.domain.model.EquipmentType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
class GetEquipmentTypesService implements GetEquipmentTypesUseCase {

    @Override
    @Transactional(readOnly = true)
    public List<EquipmentType> findAll() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<EquipmentType> findBySlug(String slug) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
