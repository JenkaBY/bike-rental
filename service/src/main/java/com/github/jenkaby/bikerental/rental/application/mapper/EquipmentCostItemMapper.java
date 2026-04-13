package com.github.jenkaby.bikerental.rental.application.mapper;

import com.github.jenkaby.bikerental.equipment.EquipmentInfo;
import com.github.jenkaby.bikerental.tariff.EquipmentCostItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper
public interface EquipmentCostItemMapper {

    @Mapping(target = "equipmentType", source = "typeSlug")
    EquipmentCostItem toEquipmentCostItem(EquipmentInfo equipmentInfo);

    List<EquipmentCostItem> toEquipmentCostItems(List<EquipmentInfo> equipments);
}
