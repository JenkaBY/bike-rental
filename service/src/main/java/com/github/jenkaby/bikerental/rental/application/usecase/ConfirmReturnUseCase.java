package com.github.jenkaby.bikerental.rental.application.usecase;

import com.github.jenkaby.bikerental.rental.application.usecase.ReturnEquipmentUseCase.ReturnEquipmentResult;
import org.jspecify.annotations.NonNull;

import java.util.UUID;

public interface ConfirmReturnUseCase {

    @NonNull
    ReturnEquipmentResult execute(@NonNull ConfirmReturnCommand command);

    record ConfirmReturnCommand(
            Long rentalId,
            UUID quoteId,
            String operatorId
    ) {
    }
}
