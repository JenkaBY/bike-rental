package com.github.jenkaby.bikerental.rental.web.query.mapper;

import com.github.jenkaby.bikerental.rental.domain.model.AvailableForRentalEquipment;
import com.github.jenkaby.bikerental.rental.web.query.dto.AvailableEquipmentResponse;
import org.mapstruct.Mapper;

@Mapper
public interface RentalAvailabilityQueryMapper {

    AvailableEquipmentResponse toResponse(AvailableForRentalEquipment equipment);
}