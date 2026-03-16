package com.github.jenkaby.bikerental.rental.application.usecase;

import com.github.jenkaby.bikerental.finance.PaymentMethod;
import org.jspecify.annotations.NonNull;

import java.util.List;

public interface ReturnEquipmentUseCase {

    @NonNull
    ReturnEquipmentResult execute(@NonNull ReturnEquipmentCommand command);

    record ReturnEquipmentCommand(
            Long rentalId,
            List<Long> equipmentIds,
            List<String> equipmentUids,
            PaymentMethod paymentMethod,
            String operatorId
    ) {
    }
}
