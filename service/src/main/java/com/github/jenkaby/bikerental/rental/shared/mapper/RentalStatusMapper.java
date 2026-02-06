package com.github.jenkaby.bikerental.rental.shared.mapper;

import com.github.jenkaby.bikerental.rental.domain.model.RentalStatus;
import org.mapstruct.Mapper;


@Mapper
public interface RentalStatusMapper {

    default RentalStatus stringToStatus(String status) {
        return status != null ? RentalStatus.valueOf(status) : null;
    }

    default String statusToString(RentalStatus status) {
        return status != null ? status.name() : null;
    }
}
