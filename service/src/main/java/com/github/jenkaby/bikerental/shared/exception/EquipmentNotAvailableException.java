package com.github.jenkaby.bikerental.shared.exception;

import lombok.Getter;
import org.jspecify.annotations.NonNull;

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


    public EquipmentNotAvailableException(@NonNull Long equipmentId, String currentStatus) {
        super(MESSAGE_TEMPLATE.formatted(equipmentId, currentStatus), ERROR_CODE, new EquipmentDetails(equipmentId.toString(), currentStatus));
    }

    public EquipmentDetails getDetails() {
        return getParams().map(params -> (EquipmentDetails) params)
                .orElseThrow(() -> new IllegalArgumentException("Expected EquipmentDetails in exception parameters"));
    }

    public record EquipmentDetails(String identifier, String status) {
    }
}
