package com.github.jenkaby.bikerental.equipment.application.service;

import com.github.jenkaby.bikerental.equipment.application.mapper.EquipmentTypeCommandToDomainMapper;
import com.github.jenkaby.bikerental.equipment.application.usecase.UpdateEquipmentTypeUseCase;
import com.github.jenkaby.bikerental.equipment.domain.model.EquipmentType;
import com.github.jenkaby.bikerental.equipment.domain.repository.EquipmentTypeRepository;
import com.github.jenkaby.bikerental.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateEquipmentTypeService implements UpdateEquipmentTypeUseCase {

    private final EquipmentTypeRepository repository;
    private final EquipmentTypeCommandToDomainMapper mapper;

    UpdateEquipmentTypeService(EquipmentTypeRepository repository, EquipmentTypeCommandToDomainMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public EquipmentType execute(UpdateEquipmentTypeCommand command) {
        EquipmentType loaded = repository.findBySlug(command.slug())
                .orElseThrow(() -> new ResourceNotFoundException(EquipmentType.class.getSimpleName(), command.slug()));

        EquipmentType updated = mapper.toEquipmentType(loaded.getId(), command);

        return repository.save(updated);
    }
}
