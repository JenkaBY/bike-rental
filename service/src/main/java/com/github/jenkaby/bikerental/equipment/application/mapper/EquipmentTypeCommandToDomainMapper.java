package com.github.jenkaby.bikerental.equipment.application.mapper;

import com.github.jenkaby.bikerental.equipment.application.usecase.CreateEquipmentTypeUseCase;
import com.github.jenkaby.bikerental.equipment.application.usecase.UpdateEquipmentTypeUseCase;
import com.github.jenkaby.bikerental.equipment.domain.model.EquipmentType;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface EquipmentTypeCommandToDomainMapper {
    EquipmentType toEquipmentType(CreateEquipmentTypeUseCase.CreateEquipmentTypeCommand command);

    EquipmentType toEquipmentType(UpdateEquipmentTypeUseCase.UpdateEquipmentTypeCommand command);
}
