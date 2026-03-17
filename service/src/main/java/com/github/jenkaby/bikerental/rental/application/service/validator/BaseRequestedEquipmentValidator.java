package com.github.jenkaby.bikerental.rental.application.service.validator;

import com.github.jenkaby.bikerental.equipment.EquipmentInfo;
import com.github.jenkaby.bikerental.shared.exception.EquipmentNotAvailableException;
import com.github.jenkaby.bikerental.shared.exception.ReferenceNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class BaseRequestedEquipmentValidator implements RequestedEquipmentValidator {

    @Override
    public void validateAvailability(List<EquipmentInfo> equipments) {
        List<EquipmentInfo> notAvailable = equipments.stream().filter(eq -> !eq.isAvailable()).toList();
        if (!CollectionUtils.isEmpty(notAvailable)) {
            throw new EquipmentNotAvailableException(notAvailable.getFirst().id(), notAvailable.getFirst().statusSlug());
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
}
