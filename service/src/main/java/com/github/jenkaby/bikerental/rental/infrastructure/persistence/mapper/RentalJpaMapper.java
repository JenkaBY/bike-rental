package com.github.jenkaby.bikerental.rental.infrastructure.persistence.mapper;

import com.github.jenkaby.bikerental.rental.domain.model.Rental;
import com.github.jenkaby.bikerental.rental.infrastructure.persistence.entity.RentalJpaEntity;
import com.github.jenkaby.bikerental.rental.shared.mapper.RentalStatusMapper;
import com.github.jenkaby.bikerental.shared.mapper.DurationMapper;
import com.github.jenkaby.bikerental.shared.mapper.InstantMapper;
import com.github.jenkaby.bikerental.shared.mapper.MoneyMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = {MoneyMapper.class, InstantMapper.class, RentalStatusMapper.class, DurationMapper.class})
public interface RentalJpaMapper {

    @Mapping(target = "plannedDuration", source = "entity.plannedDurationMinutes")
    @Mapping(target = "actualDuration", source = "entity.actualDurationMinutes")
    Rental toDomain(RentalJpaEntity entity);


    @Mapping(target = "actualDurationMinutes", source = "rental.actualDuration")
    @Mapping(target = "plannedDurationMinutes", source = "rental.plannedDuration")
    RentalJpaEntity toEntity(Rental rental);
}
