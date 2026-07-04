package com.github.jenkaby.bikerental.rental.application.usecase;

import com.github.jenkaby.bikerental.rental.domain.model.Rental;

public interface PrepareSigningUseCase {

    Rental execute(PrepareSigningCommand command);

    record PrepareSigningCommand(Long rentalId, String operatorId) {
    }
}
