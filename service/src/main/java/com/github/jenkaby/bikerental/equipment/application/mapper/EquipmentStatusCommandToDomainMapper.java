package com.github.jenkaby.bikerental.equipment.application.mapper;

import com.github.jenkaby.bikerental.equipment.application.usecase.CreateEquipmentStatusUseCase;
import com.github.jenkaby.bikerental.equipment.application.usecase.UpdateEquipmentStatusUseCase;
import com.github.jenkaby.bikerental.equipment.domain.model.EquipmentStatus;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface EquipmentStatusCommandToDomainMapper {
    EquipmentStatus toEquipmentStatus(CreateEquipmentStatusUseCase.CreateEquipmentStatusCommand command);

    EquipmentStatus toEquipmentStatus(UpdateEquipmentStatusUseCase.UpdateEquipmentStatusCommand command);
}
