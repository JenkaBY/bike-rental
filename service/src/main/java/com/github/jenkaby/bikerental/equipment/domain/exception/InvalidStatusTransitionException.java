package com.github.jenkaby.bikerental.equipment.domain.exception;

import com.github.jenkaby.bikerental.shared.exception.BikeRentalException;
import lombok.Getter;

@Getter
public class InvalidStatusTransitionException extends BikeRentalException {

    public static final String ERROR_CODE = "equipment.status.invalid_transition";

    private static final String MESSAGE_TEMPLATE = "Invalid status transition from '%s' to '%s' for equipment with id %s";

    private final Object id;
    private final String fromStatusSlug;
    private final String toStatusSlug;

    public InvalidStatusTransitionException(Object equipmentId, String fromStatusSlug, String toStatusSlug) {
        super(MESSAGE_TEMPLATE.formatted(fromStatusSlug, toStatusSlug, equipmentId), ERROR_CODE);
        this.id = equipmentId;
        this.fromStatusSlug = fromStatusSlug;
        this.toStatusSlug = toStatusSlug;
    }
}
