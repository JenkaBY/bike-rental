package com.github.jenkaby.bikerental.equipment.application.service;

import com.github.jenkaby.bikerental.equipment.application.usecase.CreateEquipmentUseCase;
import com.github.jenkaby.bikerental.equipment.domain.model.Equipment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class CreateEquipmentService implements CreateEquipmentUseCase {

    @Override
    @Transactional
    public Equipment execute(CreateEquipmentCommand command) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
