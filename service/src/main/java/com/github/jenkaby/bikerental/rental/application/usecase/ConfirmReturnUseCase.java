package com.github.jenkaby.bikerental.rental.application.usecase;

import com.github.jenkaby.bikerental.rental.application.usecase.ReturnEquipmentUseCase.ReturnEquipmentResult;
import com.github.jenkaby.bikerental.shared.domain.QuoteRef;
import org.jspecify.annotations.NonNull;

public interface ConfirmReturnUseCase {

    @NonNull
    ReturnEquipmentResult execute(@NonNull ConfirmReturnCommand command);

    record ConfirmReturnCommand(
            Long rentalId,
            QuoteRef quoteId,
            String operatorId
    ) {
    }
}
