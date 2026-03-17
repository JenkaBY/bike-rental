package com.github.jenkaby.bikerental.rental.infrastructure.persistence.mapper;

import com.github.jenkaby.bikerental.rental.domain.model.RentalEquipment;
import com.github.jenkaby.bikerental.rental.infrastructure.persistence.entity.RentalEquipmentJpaEntity;
import com.github.jenkaby.bikerental.rental.shared.mapper.RentalEquipmentStatusMapper;
import com.github.jenkaby.bikerental.shared.mapper.InstantMapper;
import com.github.jenkaby.bikerental.shared.mapper.MoneyMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = {MoneyMapper.class, InstantMapper.class, RentalEquipmentStatusMapper.class})
public interface RentalEquipmentJpaMapper {

    RentalEquipment toDomain(RentalEquipmentJpaEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "rental", ignore = true)
    RentalEquipmentJpaEntity toEntity(RentalEquipment equipment);
}

