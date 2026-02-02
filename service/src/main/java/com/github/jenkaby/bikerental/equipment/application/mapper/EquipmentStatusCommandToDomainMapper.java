package com.github.jenkaby.bikerental.equipment.application.mapper;

import com.github.jenkaby.bikerental.equipment.application.usecase.CreateEquipmentStatusUseCase;
import com.github.jenkaby.bikerental.equipment.application.usecase.UpdateEquipmentStatusUseCase;
import com.github.jenkaby.bikerental.equipment.domain.model.EquipmentStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface EquipmentStatusCommandToDomainMapper {

    @Mapping(target = "id", ignore = true)
    EquipmentStatus toEquipmentStatus(CreateEquipmentStatusUseCase.CreateEquipmentStatusCommand command);

    @Mapping(target = "id", source = "id")
    EquipmentStatus toEquipmentStatus(Long id, UpdateEquipmentStatusUseCase.UpdateEquipmentStatusCommand command);
}
