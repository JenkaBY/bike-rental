package com.github.jenkaby.bikerental.equipment.application.service;

import com.github.jenkaby.bikerental.equipment.application.mapper.EquipmentCommandToDomainMapper;
import com.github.jenkaby.bikerental.equipment.application.usecase.UpdateEquipmentUseCase;
import com.github.jenkaby.bikerental.equipment.domain.exception.DuplicateSerialNumberException;
import com.github.jenkaby.bikerental.equipment.domain.model.Equipment;
import com.github.jenkaby.bikerental.equipment.domain.model.EquipmentStatus;
import com.github.jenkaby.bikerental.equipment.domain.model.EquipmentType;
import com.github.jenkaby.bikerental.equipment.domain.repository.EquipmentRepository;
import com.github.jenkaby.bikerental.equipment.domain.repository.EquipmentStatusRepository;
import com.github.jenkaby.bikerental.equipment.domain.repository.EquipmentTypeRepository;
import com.github.jenkaby.bikerental.equipment.shared.mapper.SerialNumberMapper;
import com.github.jenkaby.bikerental.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class UpdateEquipmentService implements UpdateEquipmentUseCase {

    private final EquipmentRepository repository;
    private final EquipmentCommandToDomainMapper mapper;
    private final SerialNumberMapper serialNumberMapper;
    private final EquipmentTypeRepository typeRepository;
    private final EquipmentStatusRepository statusRepository;

    UpdateEquipmentService(
            EquipmentRepository repository,
            EquipmentCommandToDomainMapper mapper,
            SerialNumberMapper serialNumberMapper,
            EquipmentTypeRepository typeRepository,
            EquipmentStatusRepository statusRepository) {
        this.repository = repository;
        this.mapper = mapper;
        this.serialNumberMapper = serialNumberMapper;
        this.typeRepository = typeRepository;
        this.statusRepository = statusRepository;
    }

    @Override
    @Transactional
    public Equipment execute(UpdateEquipmentCommand command) {
        var existing = repository.findById(command.id())
                .orElseThrow(() -> new ResourceNotFoundException(Equipment.class.getSimpleName(), command.id().toString()));

        var newSerial = serialNumberMapper.toSerialNumber(command.serialNumber());

        if (!existing.getSerialNumber().equals(newSerial)) {
            var found = repository.findBySerialNumber(newSerial);
            if (found.isPresent() && !found.get().getId().equals(command.id())) {
                throw new DuplicateSerialNumberException(Equipment.class.getSimpleName(), newSerial.value());
            }
        }

        // validate equipment type exists if changed
        String targetType = command.equipmentTypeSlug();
        if (targetType != null && !targetType.equals(existing.getEquipmentTypeSlug())) {
            if (!typeRepository.existsBySlug(targetType)) {
                throw new ResourceNotFoundException(EquipmentType.class.getSimpleName(), targetType);
            }
        }

        // validate status exists if changed
        String targetSlug = command.statusSlug();
        if (targetSlug != null && !targetSlug.equals(existing.getStatusSlug())) {
            if (!statusRepository.existsBySlug(targetSlug)) {
                throw new ResourceNotFoundException(EquipmentStatus.class.getSimpleName(), targetSlug);
            }
        }

        var updated = mapper.toEquipment(command);

        return repository.save(updated);
    }
}
