package com.github.jenkaby.bikerental.equipment.web.query.mapper;

import com.github.jenkaby.bikerental.equipment.domain.model.EquipmentStatus;
import com.github.jenkaby.bikerental.equipment.web.query.dto.EquipmentStatusResponse;
import org.mapstruct.Mapper;

@Mapper
public interface EquipmentStatusMapper {

    EquipmentStatusResponse toResponse(EquipmentStatus equipmentStatus);
}
