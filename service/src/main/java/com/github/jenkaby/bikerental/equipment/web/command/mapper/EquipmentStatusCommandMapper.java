package com.github.jenkaby.bikerental.equipment.web.command.mapper;

import com.github.jenkaby.bikerental.equipment.application.usecase.CreateEquipmentStatusUseCase;
import com.github.jenkaby.bikerental.equipment.application.usecase.UpdateEquipmentStatusUseCase;
import com.github.jenkaby.bikerental.equipment.web.command.dto.EquipmentStatusRequest;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface EquipmentStatusCommandMapper {
    CreateEquipmentStatusUseCase.CreateEquipmentStatusCommand toCreateCommand(EquipmentStatusRequest request);

    UpdateEquipmentStatusUseCase.UpdateEquipmentStatusCommand toUpdateCommand(String slug, EquipmentStatusRequest request);
}
