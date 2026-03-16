package com.github.jenkaby.bikerental.rental.shared.mapper;

import com.github.jenkaby.bikerental.rental.domain.model.RentalEquipmentStatus;
import org.mapstruct.Mapper;


@Mapper
public interface RentalEquipmentStatusMapper {

    default RentalEquipmentStatus stringToStatus(String status) {
        return status != null ? RentalEquipmentStatus.valueOf(status) : null;
    }

    default String statusToString(RentalEquipmentStatus status) {
        return status != null ? status.name() : null;
    }
}
