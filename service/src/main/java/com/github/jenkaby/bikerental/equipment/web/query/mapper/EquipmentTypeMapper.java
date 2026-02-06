package com.github.jenkaby.bikerental.equipment.web.query.mapper;

import com.github.jenkaby.bikerental.equipment.domain.model.EquipmentType;
import com.github.jenkaby.bikerental.equipment.web.query.dto.EquipmentTypeResponse;
import org.mapstruct.Mapper;

@Mapper
public interface EquipmentTypeMapper {

    EquipmentTypeResponse toResponse(EquipmentType equipmentType);
}
