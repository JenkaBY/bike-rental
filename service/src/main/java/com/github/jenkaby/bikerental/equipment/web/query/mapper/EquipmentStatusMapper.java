package com.github.jenkaby.bikerental.equipment.web.query.mapper;

import com.github.jenkaby.bikerental.equipment.domain.model.EquipmentStatus;
import com.github.jenkaby.bikerental.equipment.web.query.dto.EquipmentStatusResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface EquipmentStatusMapper {

    EquipmentStatusResponse toResponse(EquipmentStatus equipmentStatus);
}
