package com.github.jenkaby.bikerental.shared.exception;

import com.github.jenkaby.bikerental.shared.web.advice.ErrorCodes;
import lombok.Getter;
import org.jspecify.annotations.NonNull;

import java.util.Collection;

/**
 * Exception thrown when equipment is not in good state for rental or other operations.
 * <p>
 * This exception indicates that the equipment exists but is in a state that prevents
 * the requested operation (e.g., in maintenance, decommissioned).
 */
@Getter
public class EquipmentNotAvailableException extends BikeRentalException {

    public static final String ERROR_CODE = ErrorCodes.EQUIPMENT_NOT_IN_GOOD_STATE;

    private static final String MESSAGE_TEMPLATE = "Equipments with ids %s is not in GOOD state";

    public EquipmentNotAvailableException(@NonNull Collection<Long> equipmentIds) {
        super(MESSAGE_TEMPLATE.formatted(equipmentIds), ERROR_CODE, new EquipmentDetails(equipmentIds));
    }

    public EquipmentDetails getDetails() {
        return getParams().map(params -> (EquipmentDetails) params)
                .orElseThrow(() -> new IllegalArgumentException("Expected EquipmentDetails in exception parameters"));
    }

    public record EquipmentDetails(Collection<Long> identifiers) {
    }
}
