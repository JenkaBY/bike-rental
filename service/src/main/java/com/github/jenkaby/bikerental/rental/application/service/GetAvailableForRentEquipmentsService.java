package com.github.jenkaby.bikerental.rental.application.service;

import com.github.jenkaby.bikerental.equipment.EquipmentFacade;
import com.github.jenkaby.bikerental.equipment.EquipmentInfo;
import com.github.jenkaby.bikerental.equipment.EquipmentSearchFilter;
import com.github.jenkaby.bikerental.rental.application.mapper.AvailableForRentalEquipmentMapper;
import com.github.jenkaby.bikerental.rental.application.usecase.GetAvailableForRentEquipmentsUseCase;
import com.github.jenkaby.bikerental.rental.domain.model.AvailableForRentalEquipment;
import com.github.jenkaby.bikerental.shared.domain.model.Condition;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Page;
import com.github.jenkaby.bikerental.shared.domain.model.vo.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class GetAvailableForRentEquipmentsService implements GetAvailableForRentEquipmentsUseCase {

    private final EquipmentFacade equipmentFacade;
    private final EquipmentAvailabilityService equipmentAvailabilityService;
    private final AvailableForRentalEquipmentMapper availableForRentalEquipmentMapper;

    public GetAvailableForRentEquipmentsService(
            EquipmentFacade equipmentFacade,
            EquipmentAvailabilityService equipmentAvailabilityService,
            AvailableForRentalEquipmentMapper availableForRentalEquipmentMapper) {
        this.equipmentFacade = equipmentFacade;
        this.equipmentAvailabilityService = equipmentAvailabilityService;
        this.availableForRentalEquipmentMapper = availableForRentalEquipmentMapper;
    }

    @Override
    public Page<AvailableForRentalEquipment> getAvailableEquipments(EquipmentSearchFilter filter, PageRequest pageRequest) {
        List<EquipmentInfo> candidates = equipmentFacade.getEquipmentsByConditions(Set.of(Condition.GOOD), filter);

        if (candidates.isEmpty()) {
            return Page.empty(pageRequest);
        }

        Set<Long> candidateIds = candidates.stream()
                .map(EquipmentInfo::id)
                .collect(Collectors.toSet());

        Set<Long> unavailableIds = equipmentAvailabilityService.getUnavailableIds(candidateIds);

        List<AvailableForRentalEquipment> available = candidates.stream()
                .filter(equipmentInfo -> !unavailableIds.contains(equipmentInfo.id()))
                .map(availableForRentalEquipmentMapper::toDomain)
                .toList();

        int offset = pageRequest.offset();
        int total = available.size();
        List<AvailableForRentalEquipment> pageContent = available.subList(
                Math.min(offset, total),
                Math.min(offset + pageRequest.limit(), total)
        );

        return new Page<>(pageContent, total, pageRequest);
    }
}