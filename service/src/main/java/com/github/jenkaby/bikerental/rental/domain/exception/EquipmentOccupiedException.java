package com.github.jenkaby.bikerental.rental.domain.exception;

import com.github.jenkaby.bikerental.shared.exception.BikeRentalException;
import com.github.jenkaby.bikerental.shared.web.advice.ErrorCodes;
import lombok.Getter;

import java.util.Set;

@Getter
public class EquipmentOccupiedException extends BikeRentalException {

    private static final String MESSAGE = "Requested equipment is already occupied in an active or assigned rental";

    public EquipmentOccupiedException(Set<Long> unavailableIds) {
        super(MESSAGE, ErrorCodes.EQUIPMENT_NOT_AVAILABLE, new OccupiedEquipmentDetails(unavailableIds));
    }

    public OccupiedEquipmentDetails getDetails() {
        return getParams()
                .map(d -> (OccupiedEquipmentDetails) d)
                .orElseThrow(() -> new IllegalArgumentException("Expected OccupiedEquipmentDetails in exception parameters"));
    }

    public record OccupiedEquipmentDetails(Set<Long> unavailableIds) {
    }
}