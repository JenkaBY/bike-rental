package com.github.jenkaby.bikerental.equipment.infrastructure.persistence.mapper;

import com.github.jenkaby.bikerental.equipment.domain.model.Equipment;
import com.github.jenkaby.bikerental.equipment.infrastructure.persistence.entity.EquipmentJpaEntity;
import com.github.jenkaby.bikerental.equipment.shared.mapper.SerialNumberMapper;
import com.github.jenkaby.bikerental.equipment.shared.mapper.UidMapper;
import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {SerialNumberMapper.class, UidMapper.class}
)
@DecoratedWith(EquipmentJpaMapperDecorator.class)
public interface EquipmentJpaMapper {

    @Mapping(target = "status", ignore = true)
    Equipment toDomain(EquipmentJpaEntity entity);

    @Mapping(target = "statusSlug", source = "equipment.status.slug")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    EquipmentJpaEntity toEntity(Equipment equipment);
}
