package com.github.jenkaby.bikerental.equipment.infrastructure.persistence.mapper;

import com.github.jenkaby.bikerental.equipment.domain.model.EquipmentStatus;
import com.github.jenkaby.bikerental.equipment.infrastructure.persistence.entity.EquipmentStatusJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface EquipmentStatusJpaMapper {
    EquipmentStatus toDomain(EquipmentStatusJpaEntity entity);

    EquipmentStatusJpaEntity toEntity(EquipmentStatus equipmentStatus);
}
