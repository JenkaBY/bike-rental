package com.github.jenkaby.bikerental.equipment.application.mapper;

import com.github.jenkaby.bikerental.equipment.EquipmentInfo;
import com.github.jenkaby.bikerental.equipment.domain.model.Equipment;
import com.github.jenkaby.bikerental.equipment.shared.mapper.SerialNumberMapper;
import com.github.jenkaby.bikerental.equipment.shared.mapper.UidMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(uses = {SerialNumberMapper.class, UidMapper.class})
public interface EquipmentToInfoMapper {

    @Mapping(target = "serialNumber", source = "serialNumber")
    @Mapping(target = "uid", source = "uid")
    EquipmentInfo toEquipmentInfo(Equipment equipment);
}
