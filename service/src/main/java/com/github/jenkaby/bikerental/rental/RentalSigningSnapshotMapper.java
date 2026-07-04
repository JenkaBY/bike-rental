package com.github.jenkaby.bikerental.rental;

import com.github.jenkaby.bikerental.rental.domain.model.Rental;
import com.github.jenkaby.bikerental.rental.domain.model.RentalEquipment;
import com.github.jenkaby.bikerental.shared.mapper.DiscountMapper;
import com.github.jenkaby.bikerental.shared.mapper.MoneyMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = {MoneyMapper.class, DiscountMapper.class})
interface RentalSigningSnapshotMapper {

    @Mapping(target = "rentalId", source = "id")
    RentalSigningSnapshot toSnapshot(Rental rental);

    @Mapping(target = "equipmentTypeSlug", source = "equipmentType")
    RentalSigningSnapshot.EquipmentItem toEquipmentItem(RentalEquipment equipment);
}
