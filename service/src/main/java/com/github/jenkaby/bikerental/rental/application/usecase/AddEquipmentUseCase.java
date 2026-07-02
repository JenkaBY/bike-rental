package com.github.jenkaby.bikerental.rental.application.usecase;

import com.github.jenkaby.bikerental.rental.domain.model.Rental;
import org.jspecify.annotations.NonNull;

import java.util.List;

public interface AddEquipmentUseCase {

    @NonNull
    Rental execute(@NonNull AddEquipmentCommand command);

    record AddEquipmentCommand(
            Long rentalId,
            List<Long> equipmentIds,
            String operatorId
    ) {
    }
}
