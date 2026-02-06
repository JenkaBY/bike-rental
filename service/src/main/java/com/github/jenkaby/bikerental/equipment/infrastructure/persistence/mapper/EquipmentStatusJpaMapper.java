package com.github.jenkaby.bikerental.equipment.infrastructure.persistence.mapper;

import com.github.jenkaby.bikerental.equipment.domain.model.EquipmentStatus;
import com.github.jenkaby.bikerental.equipment.infrastructure.persistence.entity.EquipmentStatusJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface EquipmentStatusJpaMapper {

    @Mapping(source = "allowedTransitionSlugs", target = "allowedTransitions")
    EquipmentStatus toDomain(EquipmentStatusJpaEntity entity);

    @Mapping(target = "allowedTransitionSlugs", source = "allowedTransitions")
    EquipmentStatusJpaEntity toEntity(EquipmentStatus equipmentStatus);
}
