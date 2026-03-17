package com.github.jenkaby.bikerental.rental.shared.mapper;

import com.github.jenkaby.bikerental.rental.domain.model.RentalEquipment;
import com.github.jenkaby.bikerental.rental.domain.model.RentalEquipmentStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

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

    @Named("RentalEquipment.returnedEquipmentIds")
    default List<Long> mapEquipmentsToIds(List<RentalEquipment> equipments) {
        return equipments.stream()
                .filter(re -> re.getStatus() == RentalEquipmentStatus.RETURNED)
                .map(RentalEquipment::getEquipmentId)
                .toList();
    }
}
