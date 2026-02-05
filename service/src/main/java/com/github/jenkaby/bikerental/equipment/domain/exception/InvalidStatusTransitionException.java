package com.github.jenkaby.bikerental.equipment.domain.exception;

import com.github.jenkaby.bikerental.equipment.domain.model.EquipmentStatus;
import com.github.jenkaby.bikerental.shared.exception.BikeRentalException;
import lombok.Getter;

public class InvalidStatusTransitionException extends BikeRentalException {

    private static final String MESSAGE_TEMPLATE = "Invalid status transition from %s to %s of %s equipment";
    @Getter
    private final Object id;

    public InvalidStatusTransitionException(Object id, EquipmentStatus fromStatus, EquipmentStatus toStatus) {
        super(MESSAGE_TEMPLATE.formatted(fromStatus.getSlug(), toStatus.getId(), id));
        this.id = id;
    }

}
