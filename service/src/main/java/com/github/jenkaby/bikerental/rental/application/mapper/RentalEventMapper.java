package com.github.jenkaby.bikerental.rental.application.mapper;

import com.github.jenkaby.bikerental.rental.domain.model.Rental;
import com.github.jenkaby.bikerental.rental.event.RentalCreated;
import com.github.jenkaby.bikerental.shared.domain.event.RentalCompleted;
import com.github.jenkaby.bikerental.shared.domain.event.RentalStarted;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDateTime;

@Mapper
public interface RentalEventMapper {

    @Mapping(target = "rentalId", source = "id")
    @Mapping(target = "status", source = "status")
    RentalCreated toRentalCreated(Rental rental);

    @Mapping(target = "rentalId", source = "id")
    RentalStarted toRentalStarted(Rental rental);

    @Mapping(target = "rentalId", source = "rental.id")
    @Mapping(target = "equipmentId", source = "rental.equipmentId")
    RentalCompleted toRentalCompleted(Rental rental, LocalDateTime returnTime, Money finalCost);
}
