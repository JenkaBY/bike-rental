package com.github.jenkaby.bikerental.rental;

import com.github.jenkaby.bikerental.rental.domain.model.Rental;
import com.github.jenkaby.bikerental.rental.domain.model.RentalEquipment;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;

@Mapper
interface RentalSigningSnapshotMapper {

    @Mapping(target = "rentalId", source = "id")
    RentalSigningSnapshot toSnapshot(Rental rental);

    @Mapping(target = "equipmentTypeSlug", source = "equipmentType")
    RentalSigningSnapshot.EquipmentItem toEquipmentItem(RentalEquipment equipment);

    default BigDecimal toAmount(Money money) {
        return money == null ? null : money.amount();
    }
}
