package com.github.jenkaby.bikerental.rental.application.usecase;

import com.github.jenkaby.bikerental.rental.domain.model.Rental;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

public interface CreateRentalUseCase {


    Rental execute(CreateRentalCommand command);

    Rental execute(CreateDraftCommand command);

    record CreateRentalCommand(
            UUID customerId,
            Long equipmentId,
            Duration duration,
            LocalDateTime startTime,
            Long tariffId  // Optional - if null, will be auto-selected
    ) {
    }

    record CreateDraftCommand() {
    }
}
