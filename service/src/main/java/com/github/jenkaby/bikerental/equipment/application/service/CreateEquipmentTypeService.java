package com.github.jenkaby.bikerental.equipment.application.service;

import com.github.jenkaby.bikerental.equipment.application.mapper.EquipmentTypeCommandToDomainMapper;
import com.github.jenkaby.bikerental.equipment.application.usecase.CreateEquipmentTypeUseCase;
import com.github.jenkaby.bikerental.equipment.domain.model.EquipmentType;
import com.github.jenkaby.bikerental.equipment.domain.repository.EquipmentTypeRepository;
import com.github.jenkaby.bikerental.shared.exception.ResourceConflictException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateEquipmentTypeService implements CreateEquipmentTypeUseCase {

    private final EquipmentTypeRepository repository;
    private final EquipmentTypeCommandToDomainMapper mapper;

    CreateEquipmentTypeService(EquipmentTypeRepository repository,
                               EquipmentTypeCommandToDomainMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public EquipmentType execute(CreateEquipmentTypeCommand command) {
        if (repository.existsBySlug(command.slug())) {
            throw new ResourceConflictException(EquipmentType.class, command.slug());
        }

        EquipmentType entity = mapper.toEquipmentType(command);
        return repository.save(entity);
    }
}
