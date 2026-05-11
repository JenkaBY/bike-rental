package com.github.jenkaby.bikerental.rental.application.mapper;

import com.github.jenkaby.bikerental.equipment.EquipmentInfo;
import com.github.jenkaby.bikerental.rental.domain.model.AvailableForRentalEquipment;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper
public interface AvailableForRentalEquipmentMapper {

    AvailableForRentalEquipment toDomain(EquipmentInfo equipmentInfo);

    List<AvailableForRentalEquipment> toDomainList(List<EquipmentInfo> equipmentInfos);
}