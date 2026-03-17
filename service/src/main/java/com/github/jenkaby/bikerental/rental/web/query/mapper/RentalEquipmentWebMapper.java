package com.github.jenkaby.bikerental.rental.web.query.mapper;

import com.github.jenkaby.bikerental.rental.domain.model.RentalEquipment;
import com.github.jenkaby.bikerental.rental.shared.mapper.RentalEquipmentStatusMapper;
import com.github.jenkaby.bikerental.rental.web.query.dto.EquipmentItemResponse;
import com.github.jenkaby.bikerental.shared.mapper.InstantMapper;
import com.github.jenkaby.bikerental.shared.mapper.MoneyMapper;
import org.mapstruct.Mapper;

@Mapper(uses = {MoneyMapper.class, InstantMapper.class, RentalEquipmentStatusMapper.class})
public interface RentalEquipmentWebMapper {

    EquipmentItemResponse toEquipmentItemResponse(RentalEquipment equipment);
}
