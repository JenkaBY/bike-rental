package com.github.jenkaby.bikerental.equipment.infrastructure.persistence.mapper;

import com.github.jenkaby.bikerental.equipment.domain.model.Equipment;
import com.github.jenkaby.bikerental.equipment.infrastructure.persistence.entity.EquipmentJpaEntity;
import com.github.jenkaby.bikerental.equipment.shared.mapper.SerialNumberMapper;
import com.github.jenkaby.bikerental.equipment.shared.mapper.UidMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = {SerialNumberMapper.class, UidMapper.class})
public interface EquipmentJpaMapper {

    Equipment toDomain(EquipmentJpaEntity entity);

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    EquipmentJpaEntity toEntity(Equipment equipment);
}
