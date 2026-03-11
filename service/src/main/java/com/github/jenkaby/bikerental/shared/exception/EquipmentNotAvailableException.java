package com.github.jenkaby.bikerental.shared.exception;

import lombok.Getter;

/**
 * Exception thrown when equipment is not available for rental or other operations.
 * <p>
 * This exception indicates that the equipment exists but is in a state that prevents
 * the requested operation (e.g., already rented, in maintenance, decommissioned).
 */
@Getter
public class EquipmentNotAvailableException extends BikeRentalException {

    public static final String ERROR_CODE = "shared.equipment.not_available";

    private static final String MESSAGE_TEMPLATE = "Equipment with id %s is not available. Current status: %s";

    private final Long equipmentId;
    private final String currentStatus;

    public EquipmentNotAvailableException(Long equipmentId, String currentStatus) {
        super(MESSAGE_TEMPLATE.formatted(equipmentId, currentStatus), ERROR_CODE);
        this.equipmentId = equipmentId;
        this.currentStatus = currentStatus;
    }
}
