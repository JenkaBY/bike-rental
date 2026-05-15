package com.github.jenkaby.bikerental.equipment.application.service;

import com.github.jenkaby.bikerental.equipment.application.mapper.EquipmentCommandToDomainMapper;
import com.github.jenkaby.bikerental.equipment.application.usecase.UpdateEquipmentUseCase;
import com.github.jenkaby.bikerental.equipment.domain.exception.DuplicateUidException;
import com.github.jenkaby.bikerental.equipment.domain.model.Equipment;
import com.github.jenkaby.bikerental.equipment.domain.model.EquipmentType;
import com.github.jenkaby.bikerental.equipment.domain.repository.EquipmentRepository;
import com.github.jenkaby.bikerental.equipment.domain.repository.EquipmentTypeRepository;
import com.github.jenkaby.bikerental.equipment.domain.service.StatusTransitionPolicy;
import com.github.jenkaby.bikerental.equipment.shared.mapper.UidMapper;
import com.github.jenkaby.bikerental.shared.exception.ReferenceNotFoundException;
import com.github.jenkaby.bikerental.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
class UpdateEquipmentService implements UpdateEquipmentUseCase {

    private final EquipmentRepository repository;
    private final EquipmentCommandToDomainMapper mapper;
    private final UidMapper uidMapper;
    private final EquipmentTypeRepository typeRepository;
    private final StatusTransitionPolicy statusTransitionPolicy;

    @Override
    @Transactional
    public Equipment execute(UpdateEquipmentCommand command) {
        var existing = repository.findById(command.id())
                .orElseThrow(() -> new ResourceNotFoundException(Equipment.class.getSimpleName(), command.id().toString()));

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
        String newStatusSlug = command.statusSlug();
        if (!newStatusSlug.equals(existing.getStatusSlug())) {
            log.info("Changing status of equipment with id {} from {} to {}", existing.getId(), existing.getStatusSlug(), newStatusSlug);
            existing.changeStatusTo(newStatusSlug, statusTransitionPolicy);
        }
        var updated = mapper.toEquipment(existing, command);

        return repository.save(updated);
    }
}
