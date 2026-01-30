package com.github.jenkaby.bikerental.equipment.application.service;

import com.github.jenkaby.bikerental.equipment.application.usecase.GetEquipmentByIdUseCase;
import com.github.jenkaby.bikerental.equipment.domain.model.Equipment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
class GetEquipmentByIdService implements GetEquipmentByIdUseCase {

    @Override
    @Transactional(readOnly = true)
    public Optional<Equipment> execute(Long id) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
