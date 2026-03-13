package com.github.jenkaby.bikerental.rental.shared.mapper;

import com.github.jenkaby.bikerental.rental.domain.model.RentalEquipment;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper
public interface RentalEquipmentMapper {

    default Long toEquipmentId(RentalEquipment rentalEquipment) {
        if (rentalEquipment == null) {
            return null;
        }
        return rentalEquipment.getEquipmentId();
    }

    List<Long> toEquipmentIds(List<RentalEquipment> rentalEquipment);
}
