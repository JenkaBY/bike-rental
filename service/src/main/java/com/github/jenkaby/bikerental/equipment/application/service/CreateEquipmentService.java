package com.github.jenkaby.bikerental.equipment.application.service;

import com.github.jenkaby.bikerental.equipment.application.mapper.EquipmentCommandToDomainMapper;
import com.github.jenkaby.bikerental.equipment.application.usecase.CreateEquipmentUseCase;
import com.github.jenkaby.bikerental.equipment.domain.exception.DuplicateSerialNumberException;
import com.github.jenkaby.bikerental.equipment.domain.model.Equipment;
import com.github.jenkaby.bikerental.equipment.domain.repository.EquipmentRepository;
import com.github.jenkaby.bikerental.equipment.shared.mapper.SerialNumberMapper;
import com.github.jenkaby.bikerental.equipment.shared.mapper.UidMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class CreateEquipmentService implements CreateEquipmentUseCase {

    private final EquipmentRepository repository;
    private final EquipmentCommandToDomainMapper mapper;
    private final SerialNumberMapper serialNumberMapper;
    private final UidMapper uidMapper;

    CreateEquipmentService(
            EquipmentRepository repository,
            EquipmentCommandToDomainMapper mapper,
            SerialNumberMapper serialNumberMapper, UidMapper uidMapper) {
        this.repository = repository;
        this.mapper = mapper;
        this.serialNumberMapper = serialNumberMapper;
        this.uidMapper = uidMapper;
    }

    @Override
    @Transactional
    public Equipment execute(CreateEquipmentCommand command) {
        var serialNumber = serialNumberMapper.toSerialNumber(command.serialNumber());
        if (repository.existsBySerialNumber(serialNumber)) {
            throw new DuplicateSerialNumberException(Equipment.class, serialNumber.value());
        }

        var uid = uidMapper.toUid(command.uid());
        if (repository.existsByUid(uid)) {
            throw new DuplicateSerialNumberException(Equipment.class, uid.value());
        }

        Equipment equipment = mapper.toEquipment(command);

        return repository.save(equipment);
    }
}
