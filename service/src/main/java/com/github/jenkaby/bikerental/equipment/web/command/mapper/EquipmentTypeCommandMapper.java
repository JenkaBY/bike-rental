package com.github.jenkaby.bikerental.equipment.web.command.mapper;

import com.github.jenkaby.bikerental.equipment.application.usecase.CreateEquipmentTypeUseCase;
import com.github.jenkaby.bikerental.equipment.application.usecase.UpdateEquipmentTypeUseCase;
import com.github.jenkaby.bikerental.equipment.web.command.dto.EquipmentTypeRequest;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface EquipmentTypeCommandMapper {
    CreateEquipmentTypeUseCase.CreateEquipmentTypeCommand toCreateCommand(EquipmentTypeRequest request);

    UpdateEquipmentTypeUseCase.UpdateEquipmentTypeCommand toUpdateCommand(String slug, EquipmentTypeRequest request);
}
