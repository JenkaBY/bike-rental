package com.github.jenkaby.bikerental.equipment.application.service;

import com.github.jenkaby.bikerental.equipment.application.mapper.EquipmentCommandToDomainMapper;
import com.github.jenkaby.bikerental.equipment.application.usecase.CreateEquipmentUseCase;
import com.github.jenkaby.bikerental.equipment.domain.exception.DuplicateSerialNumberException;
import com.github.jenkaby.bikerental.equipment.domain.model.Equipment;
import com.github.jenkaby.bikerental.equipment.domain.model.EquipmentStatus;
import com.github.jenkaby.bikerental.equipment.domain.repository.EquipmentRepository;
import com.github.jenkaby.bikerental.equipment.domain.repository.EquipmentStatusRepository;
import com.github.jenkaby.bikerental.equipment.shared.mapper.UidMapper;
import com.github.jenkaby.bikerental.shared.exception.ReferenceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
class CreateEquipmentService implements CreateEquipmentUseCase {

    private final EquipmentRepository repository;
    private final EquipmentCommandToDomainMapper mapper;
    private final UidMapper uidMapper;
    private final EquipmentStatusRepository statusRepository;

    @Override
    @Transactional
    public Equipment execute(CreateEquipmentCommand command) {
        var uid = uidMapper.toUid(command.uid());
        if (repository.existsByUid(uid)) {
            throw new DuplicateSerialNumberException(Equipment.class, uid.value());
        }

        if (!statusRepository.existsBySlug(command.statusSlug())) {
            throw new ReferenceNotFoundException(EquipmentStatus.class, command.statusSlug());
        }

        Equipment equipment = mapper.toEquipment(command);

        return repository.save(equipment);
    }
}
