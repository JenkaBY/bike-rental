package com.github.jenkaby.bikerental.rental.application.usecase;

import com.github.jenkaby.bikerental.rental.domain.model.Rental;

public interface CancelRentalUseCase {

    Rental execute(CancelCommand command);

    record CancelCommand(Long rentalId, String operatorId) {
    }
}