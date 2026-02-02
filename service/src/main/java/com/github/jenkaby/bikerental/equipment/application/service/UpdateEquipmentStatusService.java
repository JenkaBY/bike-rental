package com.github.jenkaby.bikerental.equipment.application.service;

import com.github.jenkaby.bikerental.equipment.application.mapper.EquipmentStatusCommandToDomainMapper;
import com.github.jenkaby.bikerental.equipment.application.usecase.UpdateEquipmentStatusUseCase;
import com.github.jenkaby.bikerental.equipment.domain.model.EquipmentStatus;
import com.github.jenkaby.bikerental.equipment.domain.repository.EquipmentStatusRepository;
import com.github.jenkaby.bikerental.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateEquipmentStatusService implements UpdateEquipmentStatusUseCase {

    private final EquipmentStatusRepository repository;
    private final EquipmentStatusCommandToDomainMapper mapper;

    UpdateEquipmentStatusService(EquipmentStatusRepository repository,
                                 EquipmentStatusCommandToDomainMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public EquipmentStatus execute(UpdateEquipmentStatusCommand command) {
        // ensure the status exists
        EquipmentStatus loaded = repository.findBySlug(command.slug())
                .orElseThrow(() -> new ResourceNotFoundException(EquipmentStatus.class.getSimpleName(), command.slug()));

        EquipmentStatus updated = mapper.toEquipmentStatus(loaded.getId(), command);

        return repository.save(updated);
    }
}
