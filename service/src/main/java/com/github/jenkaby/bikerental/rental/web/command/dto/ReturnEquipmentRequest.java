package com.github.jenkaby.bikerental.rental.web.command.dto;

import com.github.jenkaby.bikerental.finance.PaymentMethod;
import jakarta.validation.constraints.AssertTrue;

public record ReturnEquipmentRequest(
        Long rentalId,
        Long equipmentId,
        String equipmentUid,
        PaymentMethod paymentMethod,
        String operatorId
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
