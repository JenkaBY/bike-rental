package com.github.jenkaby.bikerental.rental.application.usecase;

import com.github.jenkaby.bikerental.finance.PaymentMethod;
import org.jspecify.annotations.NonNull;

public interface ReturnEquipmentUseCase {

    @NonNull
    ReturnEquipmentResult execute(@NonNull ReturnEquipmentCommand command);

    record ReturnEquipmentCommand(
            Long rentalId,
            Long equipmentId,
            String equipmentUid,
            PaymentMethod paymentMethod,
            String operatorId
    ) {
    }
}
