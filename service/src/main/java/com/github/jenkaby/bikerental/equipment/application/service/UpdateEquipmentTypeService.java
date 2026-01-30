package com.github.jenkaby.bikerental.equipment.application.service;

import com.github.jenkaby.bikerental.equipment.application.usecase.UpdateEquipmentTypeUseCase;
import com.github.jenkaby.bikerental.equipment.domain.model.EquipmentType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateEquipmentTypeService implements UpdateEquipmentTypeUseCase {
    @Override
    @Transactional
    public EquipmentType execute(UpdateEquipmentTypeCommand command) {
        // TODO: Implement update logic
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
