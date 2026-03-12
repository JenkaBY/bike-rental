package com.github.jenkaby.bikerental.equipment.domain.exception;

import com.github.jenkaby.bikerental.shared.exception.BikeRentalException;
import lombok.Getter;

@Getter
public class InvalidStatusTransitionException extends BikeRentalException {

    public static final String ERROR_CODE = "equipment.status.invalid_transition";

    private static final String MESSAGE_TEMPLATE = "Invalid status transition from '%s' to '%s' for equipment with id %s";

    public InvalidStatusTransitionException(Object equipmentId, String fromStatusSlug, String toStatusSlug) {
        this(new InvalidStatusTransitionException.StatusTransitionDetails(equipmentId, fromStatusSlug, toStatusSlug));
    }

    public InvalidStatusTransitionException(StatusTransitionDetails details) {
        super(MESSAGE_TEMPLATE.formatted(details.fromStatus(), details.toStatus(), details.id()), ERROR_CODE, details);
    }

    public StatusTransitionDetails getDetails() {
        return super.getParams().map(params -> (StatusTransitionDetails) params)
                .orElseThrow(() -> new IllegalArgumentException("InvalidStatusTransitionException must have StatusTransitionDetails as params"));
    }

    public record StatusTransitionDetails(Object id, String fromStatus, String toStatus) {
    }
}
