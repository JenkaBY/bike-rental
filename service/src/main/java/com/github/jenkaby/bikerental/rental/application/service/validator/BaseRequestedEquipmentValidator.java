package com.github.jenkaby.bikerental.rental.application.service.validator;

import com.github.jenkaby.bikerental.equipment.EquipmentInfo;
import com.github.jenkaby.bikerental.rental.application.service.EquipmentAvailabilityService;
import com.github.jenkaby.bikerental.rental.domain.exception.EquipmentOccupiedException;
import com.github.jenkaby.bikerental.shared.exception.EquipmentNotAvailableException;
import com.github.jenkaby.bikerental.shared.exception.ReferenceNotFoundException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Component
public class BaseRequestedEquipmentValidator implements RequestedEquipmentValidator {

    private final EquipmentAvailabilityService equipmentAvailabilityService;

    public BaseRequestedEquipmentValidator(EquipmentAvailabilityService equipmentAvailabilityService) {
        this.equipmentAvailabilityService = equipmentAvailabilityService;
    }

    @Override
    public void validateAvailability(List<EquipmentInfo> equipments) {
        Set<Long> candidateIds = equipments.stream()
                .map(EquipmentInfo::id)
                .collect(Collectors.toSet());
        Set<Long> unavailableIds = equipmentAvailabilityService.getUnavailableIds(candidateIds);
        if (!unavailableIds.isEmpty()) {
            throw new EquipmentOccupiedException(unavailableIds);
        }
    }

    @Override
    public void validateSize(List<Long> requestedEquipmentIds, List<EquipmentInfo> equipments) {
        if (equipments.size() != requestedEquipmentIds.size()) {
            var foundIds = equipments.stream()
                    .map(EquipmentInfo::id)
                    .collect(Collectors.toCollection(ArrayList::new));
            var missingIds = new ArrayList<>(requestedEquipmentIds);
            missingIds.removeAll(foundIds);
            throw new ReferenceNotFoundException("Equipment", missingIds.toString());
        }
    }

    @Override
    public void validateEquipmentsCondition(List<EquipmentInfo> equipments) {
        Set<Long> brokenEquipmentIds = equipments.stream()
                .filter(Predicate.not(EquipmentInfo::isAvailableForRental))
                .map(EquipmentInfo::id)
                .collect(Collectors.toSet());
        if (!brokenEquipmentIds.isEmpty()) {
            throw new EquipmentNotAvailableException(brokenEquipmentIds);
        }
    }
}
