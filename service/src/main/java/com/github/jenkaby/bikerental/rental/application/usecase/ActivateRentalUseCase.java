package com.github.jenkaby.bikerental.rental.application.usecase;

import com.github.jenkaby.bikerental.rental.domain.model.Rental;

public interface ActivateRentalUseCase {

    Rental execute(ActivateCommand command);

    record ActivateCommand(Long rentalId, String operatorId) {
    }
}