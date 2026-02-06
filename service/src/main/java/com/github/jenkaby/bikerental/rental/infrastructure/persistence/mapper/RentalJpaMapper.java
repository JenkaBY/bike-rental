package com.github.jenkaby.bikerental.rental.infrastructure.persistence.mapper;

import com.github.jenkaby.bikerental.rental.domain.model.Rental;
import com.github.jenkaby.bikerental.rental.infrastructure.persistence.entity.RentalJpaEntity;
import com.github.jenkaby.bikerental.rental.shared.mapper.RentalStatusMapper;
import com.github.jenkaby.bikerental.shared.mapper.DurationMapper;
import com.github.jenkaby.bikerental.shared.mapper.InstantMapper;
import com.github.jenkaby.bikerental.shared.mapper.MoneyMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {MoneyMapper.class, InstantMapper.class, RentalStatusMapper.class, DurationMapper.class},
        injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public interface RentalJpaMapper {

    Rental toDomain(RentalJpaEntity entity);

    RentalJpaEntity toEntity(Rental rental);
}
