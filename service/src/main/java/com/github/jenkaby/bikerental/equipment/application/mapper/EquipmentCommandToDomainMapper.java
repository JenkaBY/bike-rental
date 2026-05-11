package com.github.jenkaby.bikerental.equipment.application.mapper;

import com.github.jenkaby.bikerental.equipment.application.usecase.CreateEquipmentUseCase;
import com.github.jenkaby.bikerental.equipment.application.usecase.UpdateEquipmentUseCase;
import com.github.jenkaby.bikerental.equipment.domain.model.Equipment;
import com.github.jenkaby.bikerental.equipment.shared.mapper.SerialNumberMapper;
import com.github.jenkaby.bikerental.equipment.shared.mapper.UidMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;


@Mapper(uses = {SerialNumberMapper.class, UidMapper.class})
public interface EquipmentCommandToDomainMapper {
    //TODO update conditions slug
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "conditionSlug", ignore = true)
    Equipment toEquipment(CreateEquipmentUseCase.CreateEquipmentCommand command);

    //TODO update conditions slug
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "conditionSlug", ignore = true)
    Equipment toEquipment(@MappingTarget Equipment toUpdate, UpdateEquipmentUseCase.UpdateEquipmentCommand command);

    @Mapping(target = "statusSlug", source = "newStatusSlug")
    UpdateEquipmentUseCase.UpdateEquipmentCommand toUpdateCommand(Equipment equipment, String newStatusSlug);
}
