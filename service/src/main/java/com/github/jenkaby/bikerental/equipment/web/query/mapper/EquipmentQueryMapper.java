package com.github.jenkaby.bikerental.equipment.web.query.mapper;

import com.github.jenkaby.bikerental.equipment.application.usecase.SearchEquipmentsUseCase;
import com.github.jenkaby.bikerental.equipment.domain.model.Equipment;
import com.github.jenkaby.bikerental.equipment.shared.mapper.SerialNumberMapper;
import com.github.jenkaby.bikerental.equipment.shared.mapper.UidMapper;
import com.github.jenkaby.bikerental.equipment.web.query.dto.EquipmentResponse;
import com.github.jenkaby.bikerental.shared.mapper.PageMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Pageable;

import java.util.List;


@Mapper(uses = {UidMapper.class, SerialNumberMapper.class, PageMapper.class})
public interface EquipmentQueryMapper {

    @Mapping(target = "status", source = "statusSlug")
    @Mapping(target = "type", source = "typeSlug")
    @Mapping(target = "conditionNotes", source = "condition")
    @Mapping(target = "condition", source = "conditionSlug")
    EquipmentResponse toResponse(Equipment equipment);

    List<EquipmentResponse> toResponses(List<Equipment> equipments);

    @Mapping(target = "typeSlug", source = "type")
    @Mapping(target = "statusSlug", source = "status")
    @Mapping(target = "pageRequest", source = "pageable")
    SearchEquipmentsUseCase.SearchEquipmentsQuery toSearchQuery(
            String status,
            String type,
            String searchText,
            Pageable pageable);
}
