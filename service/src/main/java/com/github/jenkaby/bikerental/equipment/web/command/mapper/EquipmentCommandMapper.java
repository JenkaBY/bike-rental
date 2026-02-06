package com.github.jenkaby.bikerental.equipment.web.command.mapper;

import com.github.jenkaby.bikerental.equipment.application.usecase.CreateEquipmentUseCase;
import com.github.jenkaby.bikerental.equipment.application.usecase.UpdateEquipmentUseCase;
import com.github.jenkaby.bikerental.equipment.web.command.dto.EquipmentRequest;
import org.mapstruct.Mapper;

@Mapper
public interface EquipmentCommandMapper {

    CreateEquipmentUseCase.CreateEquipmentCommand toCreateCommand(EquipmentRequest request);

    UpdateEquipmentUseCase.UpdateEquipmentCommand toUpdateCommand(Long id, EquipmentRequest request);
}
