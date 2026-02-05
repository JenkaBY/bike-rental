package com.github.jenkaby.bikerental.equipment.application.service;

import com.github.jenkaby.bikerental.equipment.application.mapper.EquipmentStatusCommandToDomainMapper;
import com.github.jenkaby.bikerental.equipment.application.usecase.CreateEquipmentStatusUseCase;
import com.github.jenkaby.bikerental.equipment.domain.model.EquipmentStatus;
import com.github.jenkaby.bikerental.equipment.domain.repository.EquipmentStatusRepository;
import com.github.jenkaby.bikerental.shared.exception.ReferenceNotFoundException;
import com.github.jenkaby.bikerental.shared.exception.ResourceConflictException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateEquipmentStatusService implements CreateEquipmentStatusUseCase {

    private final EquipmentStatusRepository repository;
    private final EquipmentStatusCommandToDomainMapper mapper;

    CreateEquipmentStatusService(EquipmentStatusRepository repository,
                                 EquipmentStatusCommandToDomainMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public EquipmentStatus execute(CreateEquipmentStatusCommand command) {
        if (repository.existsBySlug(command.slug())) {
            throw new ResourceConflictException(EquipmentStatus.class, command.slug());
        }
        for (String toSlug : command.allowedTransitions()) {
            if (!repository.existsBySlug(toSlug)) {
                throw new ReferenceNotFoundException(EquipmentStatus.class, toSlug);
            }
        }
        EquipmentStatus entity = mapper.toEquipmentStatus(command);
        return repository.save(entity);
    }
}
