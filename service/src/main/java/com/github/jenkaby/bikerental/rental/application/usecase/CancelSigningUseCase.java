package com.github.jenkaby.bikerental.rental.application.usecase;

import com.github.jenkaby.bikerental.rental.domain.model.Rental;

public interface CancelSigningUseCase {

    Rental execute(CancelSigningCommand command);

    record CancelSigningCommand(Long rentalId, String operatorId) {
    }
}
