package com.github.jenkaby.bikerental.equipment.application.service;

import com.github.jenkaby.bikerental.equipment.application.mapper.EquipmentCommandToDomainMapper;
import com.github.jenkaby.bikerental.equipment.application.usecase.UpdateEquipmentUseCase;
import com.github.jenkaby.bikerental.equipment.domain.exception.DuplicateSerialNumberException;
import com.github.jenkaby.bikerental.equipment.domain.exception.DuplicateUidException;
import com.github.jenkaby.bikerental.equipment.domain.model.Equipment;
import com.github.jenkaby.bikerental.equipment.domain.model.EquipmentStatus;
import com.github.jenkaby.bikerental.equipment.domain.model.EquipmentType;
import com.github.jenkaby.bikerental.equipment.domain.repository.EquipmentRepository;
import com.github.jenkaby.bikerental.equipment.domain.repository.EquipmentStatusRepository;
import com.github.jenkaby.bikerental.equipment.domain.repository.EquipmentTypeRepository;
import com.github.jenkaby.bikerental.equipment.shared.mapper.SerialNumberMapper;
import com.github.jenkaby.bikerental.equipment.shared.mapper.UidMapper;
import com.github.jenkaby.bikerental.shared.exception.ReferenceNotFoundException;
import com.github.jenkaby.bikerental.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class UpdateEquipmentService implements UpdateEquipmentUseCase {

    private final EquipmentRepository repository;
    private final EquipmentCommandToDomainMapper mapper;
    private final SerialNumberMapper serialNumberMapper;
    private final UidMapper uidMapper;
    private final EquipmentTypeRepository typeRepository;
    private final EquipmentStatusRepository statusRepository;

    UpdateEquipmentService(
            EquipmentRepository repository,
            EquipmentCommandToDomainMapper mapper,
            SerialNumberMapper serialNumberMapper,
            UidMapper uidMapper,
            EquipmentTypeRepository typeRepository,
            EquipmentStatusRepository statusRepository) {
        this.repository = repository;
        this.mapper = mapper;
        this.serialNumberMapper = serialNumberMapper;
        this.typeRepository = typeRepository;
        this.statusRepository = statusRepository;
        this.uidMapper = uidMapper;
    }

    @Override
    @Transactional
    public Equipment execute(UpdateEquipmentCommand command) {
        var existing = repository.findById(command.id())
                .orElseThrow(() -> new ResourceNotFoundException(Equipment.class.getSimpleName(), command.id().toString()));

        var newSerial = serialNumberMapper.toSerialNumber(command.serialNumber());

        if (!existing.getSerialNumber().equals(newSerial)) {
            if (repository.existsBySerialNumber(newSerial)) {
                throw new DuplicateSerialNumberException(Equipment.class, newSerial.value());
            }
        }

        var newUid = uidMapper.toUid(command.uid());
        if (!existing.getUid().equals(newUid)) {
            if (repository.existsByUid(newUid)) {
                throw new DuplicateUidException(Equipment.class, newUid.value());
            }
        }

        String newType = command.typeSlug();
        if (newType != null && !newType.equals(existing.getTypeSlug())) {
            if (!typeRepository.existsBySlug(newType)) {
                throw new ReferenceNotFoundException(EquipmentType.class, newType);
            }
        }

        // validate status exists if changed
        String newStatus = command.statusSlug();
        if (newStatus != null && !newStatus.equals(existing.getStatusSlug())) {
            if (!statusRepository.existsBySlug(newStatus)) {
                throw new ReferenceNotFoundException(EquipmentStatus.class, newStatus);
            }
        }

        var updated = mapper.toEquipment(command);

        return repository.save(updated);
    }
}
