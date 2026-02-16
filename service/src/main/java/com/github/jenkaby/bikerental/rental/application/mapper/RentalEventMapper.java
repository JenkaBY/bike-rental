package com.github.jenkaby.bikerental.rental.application.mapper;

import com.github.jenkaby.bikerental.rental.domain.model.Rental;
import com.github.jenkaby.bikerental.rental.event.RentalCreated;
import com.github.jenkaby.bikerental.shared.domain.event.RentalStarted;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface RentalEventMapper {

    @Mapping(target = "rentalId", source = "id")
    @Mapping(target = "status", source = "status")
    RentalCreated toRentalCreated(Rental rental);

    @Mapping(target = "rentalId", source = "id")
    RentalStarted toRentalStarted(Rental rental);
}
