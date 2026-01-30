package com.github.jenkaby.bikerental.equipment.application.service;

import com.github.jenkaby.bikerental.equipment.application.usecase.UpdateEquipmentUseCase;
import com.github.jenkaby.bikerental.equipment.domain.model.Equipment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class UpdateEquipmentService implements UpdateEquipmentUseCase {

    @Override
    @Transactional
    public Equipment execute(UpdateEquipmentCommand command) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
