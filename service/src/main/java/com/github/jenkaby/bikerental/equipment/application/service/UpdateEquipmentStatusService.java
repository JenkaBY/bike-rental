package com.github.jenkaby.bikerental.equipment.application.service;

import com.github.jenkaby.bikerental.equipment.application.usecase.UpdateEquipmentStatusUseCase;
import com.github.jenkaby.bikerental.equipment.domain.model.EquipmentStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateEquipmentStatusService implements UpdateEquipmentStatusUseCase {
    @Override
    @Transactional
    public EquipmentStatus execute(UpdateEquipmentStatusCommand command) {
        // TODO: Implement update logic
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
