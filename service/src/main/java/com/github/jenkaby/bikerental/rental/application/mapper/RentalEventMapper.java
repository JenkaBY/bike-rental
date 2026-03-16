package com.github.jenkaby.bikerental.rental.application.mapper;

import com.github.jenkaby.bikerental.rental.domain.model.Rental;
import com.github.jenkaby.bikerental.rental.domain.model.RentalEquipment;
import com.github.jenkaby.bikerental.rental.domain.model.RentalEquipmentStatus;
import com.github.jenkaby.bikerental.rental.event.RentalCreated;
import com.github.jenkaby.bikerental.rental.shared.mapper.RentalEquipmentMapper;
import com.github.jenkaby.bikerental.shared.domain.event.RentalCompleted;
import com.github.jenkaby.bikerental.shared.domain.event.RentalStarted;
import com.github.jenkaby.bikerental.shared.domain.event.RentalUpdated;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.LocalDateTime;
import java.util.List;

@Mapper(uses = {RentalEquipmentMapper.class})
public interface RentalEventMapper {

    @Mapping(target = "rentalId", source = "id")
    @Mapping(target = "status", source = "status")
    RentalCreated toRentalCreated(Rental rental);

    @Mapping(target = "equipmentIds", source = "equipments")
    @Mapping(target = "rentalId", source = "id")
    RentalStarted toRentalStarted(Rental rental);

    @Mapping(target = "rentalId", source = "rental.id")
    @Mapping(target = "equipmentIds", source = "rental.equipments")
    @Mapping(target = "returnedEquipmentIds", source = "rental.equipments", qualifiedByName = "RentalEquipment.returnedEquipmentIds")
    RentalCompleted toRentalCompleted(Rental rental, LocalDateTime returnTime, Money totalCost);

    @Mapping(target = "previousState", source = "previous")
    @Mapping(target = "currentState", source = "current")
    @Mapping(target = "rentalId", source = "rental.id")
    @Mapping(target = "customerId", source = "rental.customerId")
    RentalUpdated toRentalUpdated(Rental rental, RentalUpdated.RentalState previous, RentalUpdated.RentalState current);

    @Mapping(target = "rentalStatus", source = "status")
    @Mapping(target = "equipmentIds", source = "equipments", qualifiedByName = "RentalEquipment.returnedEquipmentIds")
    RentalUpdated.RentalState toRentalState(Rental source);

    @Named("RentalEquipment.returnedEquipmentIds")
    default List<Long> mapEquipmentsToIds(List<RentalEquipment> equipments) {
        return equipments.stream()
                .filter(re -> re.getStatus() == RentalEquipmentStatus.RETURNED)
                .map(RentalEquipment::getEquipmentId)
                .toList();
    }
}
