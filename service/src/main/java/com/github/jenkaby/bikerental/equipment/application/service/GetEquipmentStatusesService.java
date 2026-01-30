package com.github.jenkaby.bikerental.equipment.application.service;

import com.github.jenkaby.bikerental.equipment.application.usecase.GetEquipmentStatusesUseCase;
import com.github.jenkaby.bikerental.equipment.domain.model.EquipmentStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
class GetEquipmentStatusesService implements GetEquipmentStatusesUseCase {

    @Override
    @Transactional(readOnly = true)
    public List<EquipmentStatus> findAll() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<EquipmentStatus> findBySlug(String slug) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
