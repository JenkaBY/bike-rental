package com.github.jenkaby.bikerental.equipment.application.mapper;

import com.github.jenkaby.bikerental.equipment.application.usecase.CreateEquipmentUseCase;
import com.github.jenkaby.bikerental.equipment.application.usecase.UpdateEquipmentUseCase;
import com.github.jenkaby.bikerental.equipment.domain.model.Equipment;
import com.github.jenkaby.bikerental.equipment.shared.mapper.SerialNumberMapper;
import com.github.jenkaby.bikerental.equipment.shared.mapper.UidMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;


@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {SerialNumberMapper.class, UidMapper.class})
public interface EquipmentCommandToDomainMapper {

    @Mapping(target = "id", ignore = true)
    Equipment toEquipment(CreateEquipmentUseCase.CreateEquipmentCommand command);

    Equipment toEquipment(UpdateEquipmentUseCase.UpdateEquipmentCommand command);
}
