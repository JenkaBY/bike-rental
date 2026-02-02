package com.github.jenkaby.bikerental.equipment.application.service;

import com.github.jenkaby.bikerental.equipment.application.usecase.GetEquipmentBySerialNumberUseCase;
import com.github.jenkaby.bikerental.equipment.domain.model.Equipment;
import com.github.jenkaby.bikerental.equipment.domain.repository.EquipmentRepository;
import com.github.jenkaby.bikerental.equipment.shared.domain.model.vo.SerialNumber;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
class GetEquipmentBySerialNumberService implements GetEquipmentBySerialNumberUseCase {

    private final EquipmentRepository repository;

    GetEquipmentBySerialNumberService(EquipmentRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Equipment> execute(SerialNumber serialNumber) {
        return repository.findBySerialNumber(serialNumber);
    }
}
