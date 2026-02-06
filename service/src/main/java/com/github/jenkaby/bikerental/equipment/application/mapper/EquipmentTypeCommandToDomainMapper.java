package com.github.jenkaby.bikerental.equipment.application.mapper;

import com.github.jenkaby.bikerental.equipment.application.usecase.CreateEquipmentTypeUseCase;
import com.github.jenkaby.bikerental.equipment.application.usecase.UpdateEquipmentTypeUseCase;
import com.github.jenkaby.bikerental.equipment.domain.model.EquipmentType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface EquipmentTypeCommandToDomainMapper {
    @Mapping(target = "id", ignore = true)
    EquipmentType toEquipmentType(CreateEquipmentTypeUseCase.CreateEquipmentTypeCommand command);

    @Mapping(target = "id", source = "id")
    EquipmentType toEquipmentType(Long id, UpdateEquipmentTypeUseCase.UpdateEquipmentTypeCommand command);
}
