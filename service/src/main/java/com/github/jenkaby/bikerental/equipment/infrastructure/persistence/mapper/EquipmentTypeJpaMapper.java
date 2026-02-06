package com.github.jenkaby.bikerental.equipment.infrastructure.persistence.mapper;

import com.github.jenkaby.bikerental.equipment.domain.model.EquipmentType;
import com.github.jenkaby.bikerental.equipment.infrastructure.persistence.entity.EquipmentTypeJpaEntity;
import org.mapstruct.Mapper;

@Mapper
public interface EquipmentTypeJpaMapper {
    EquipmentType toDomain(EquipmentTypeJpaEntity entity);

    EquipmentTypeJpaEntity toEntity(EquipmentType equipmentType);
}
