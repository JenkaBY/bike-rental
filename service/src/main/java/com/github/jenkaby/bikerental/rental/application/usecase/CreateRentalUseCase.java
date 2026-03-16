package com.github.jenkaby.bikerental.rental.application.usecase;

import com.github.jenkaby.bikerental.rental.domain.model.Rental;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

public interface CreateRentalUseCase {


    Rental execute(CreateRentalCommand command);

    Rental execute(CreateDraftCommand command);

    record CreateRentalCommand(
            UUID customerId,
            List<Long> equipmentIds,
            Duration duration,
            Long tariffId  // Optional - Left it for special cases when need to apply custom tariff
    ) {
    }

    record CreateDraftCommand() {
    }
}
