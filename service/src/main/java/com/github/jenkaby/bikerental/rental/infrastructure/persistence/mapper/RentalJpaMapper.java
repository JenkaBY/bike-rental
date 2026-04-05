package com.github.jenkaby.bikerental.rental.infrastructure.persistence.mapper;

import com.github.jenkaby.bikerental.rental.domain.model.Rental;
import com.github.jenkaby.bikerental.rental.infrastructure.persistence.entity.RentalJpaEntity;
import com.github.jenkaby.bikerental.rental.shared.mapper.RentalStatusMapper;
import com.github.jenkaby.bikerental.shared.mapper.DurationMapper;
import com.github.jenkaby.bikerental.shared.mapper.InstantMapper;
import com.github.jenkaby.bikerental.shared.mapper.MoneyMapper;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(uses = {MoneyMapper.class, InstantMapper.class, RentalStatusMapper.class, DurationMapper.class, RentalEquipmentJpaMapper.class})
public interface RentalJpaMapper {

    @Mapping(target = "plannedDuration", source = "entity.plannedDurationMinutes")
    @Mapping(target = "actualDuration", source = "entity.actualDurationMinutes")
    @Mapping(target = "equipments", source = "entity.rentalEquipments")
    Rental toDomain(RentalJpaEntity entity);


    @Mapping(target = "actualDurationMinutes", source = "rental.actualDuration")
    @Mapping(target = "plannedDurationMinutes", source = "rental.plannedDuration")
    @Mapping(target = "rentalEquipments", source = "rental.equipments")
    RentalJpaEntity toEntity(Rental rental);

    @AfterMapping
    default void setRelationships(@MappingTarget RentalJpaEntity result) {
        if (result.getRentalEquipments() != null) {
            result.getRentalEquipments().forEach(equipment -> equipment.setRental(result));
        }
    }
}
