package com.github.jenkaby.bikerental.rental.shared.mapper;

import com.github.jenkaby.bikerental.rental.domain.model.RentalStatus;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;


@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface RentalStatusMapper {

    default RentalStatus stringToStatus(String status) {
        return status != null ? RentalStatus.valueOf(status) : null;
    }

    default String statusToString(RentalStatus status) {
        return status != null ? status.name() : null;
    }
}
