package com.github.jenkaby.bikerental.equipment.application.service;

import com.github.jenkaby.bikerental.equipment.application.mapper.EquipmentCommandToDomainMapper;
import com.github.jenkaby.bikerental.equipment.application.usecase.CreateEquipmentUseCase;
import com.github.jenkaby.bikerental.equipment.domain.exception.DuplicateSerialNumberException;
import com.github.jenkaby.bikerental.equipment.domain.model.Equipment;
import com.github.jenkaby.bikerental.equipment.domain.repository.EquipmentRepository;
import com.github.jenkaby.bikerental.equipment.shared.mapper.SerialNumberMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class CreateEquipmentService implements CreateEquipmentUseCase {

    private final EquipmentRepository repository;
    private final EquipmentCommandToDomainMapper mapper;
    private final SerialNumberMapper serialNumberMapper;

    CreateEquipmentService(
            EquipmentRepository repository,
            EquipmentCommandToDomainMapper mapper,
            SerialNumberMapper serialNumberMapper) {
        this.repository = repository;
        this.mapper = mapper;
        this.serialNumberMapper = serialNumberMapper;
    }

    @Override
    @Transactional
    public Equipment execute(CreateEquipmentCommand command) {
        // validate and normalize serial number
        var serialNumber = serialNumberMapper.toSerialNumber(command.serialNumber());

        if (repository.existsBySerialNumber(serialNumber)) {
            throw new DuplicateSerialNumberException(Equipment.class.getSimpleName(), serialNumber.value());
        }

        Equipment equipment = mapper.toEquipment(command);

        return repository.save(equipment);
    }
}
