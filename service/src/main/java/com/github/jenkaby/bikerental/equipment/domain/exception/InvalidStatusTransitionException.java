package com.github.jenkaby.bikerental.equipment.domain.exception;

import com.github.jenkaby.bikerental.shared.exception.BikeRentalException;
import lombok.Getter;

public class InvalidStatusTransitionException extends BikeRentalException {

    private static final String MESSAGE_TEMPLATE = "Invalid status transition from %s to %s for equipment with id %s";
    @Getter
    private final Object id;
    @Getter
    private final String fromStatusSlug;
    @Getter
    private final String toStatusSlug;

    public InvalidStatusTransitionException(Object equipmentId, String fromStatusSlug, String toStatusSlug) {
        super(MESSAGE_TEMPLATE.formatted(fromStatusSlug, toStatusSlug, equipmentId));
        this.id = equipmentId;
        this.fromStatusSlug = fromStatusSlug;
        this.toStatusSlug = toStatusSlug;
    }

}
