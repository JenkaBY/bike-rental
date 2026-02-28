package com.github.jenkaby.bikerental.rental.web.command.dto;

import com.github.jenkaby.bikerental.finance.PaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;

@Schema(description = "Request body for returning rented equipment")
public record ReturnEquipmentRequest(
        @Schema(description = "Rental ID", example = "1") Long rentalId,
        @Schema(description = "Equipment ID", example = "1") Long equipmentId,
        @Schema(description = "Equipment UID", example = "BIKE-001") String equipmentUid,
        @Schema(description = "Payment method for any additional charge") PaymentMethod paymentMethod,
        @Schema(description = "Operator identifier", example = "operator-1") String operatorId
) {
    @AssertTrue(message = "At least one of rentalId, equipmentId, or equipmentUid must be provided")
    public boolean isValidIdentifier() {
        int count = 0;
        if (rentalId != null) count++;
        if (equipmentId != null) count++;
        if (equipmentUid != null && !equipmentUid.isBlank()) count++;
        return count > 0;
    }
}
