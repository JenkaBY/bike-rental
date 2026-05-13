package com.github.jenkaby.bikerental.rental.application.usecase;

import com.github.jenkaby.bikerental.rental.domain.model.Rental;
import com.github.jenkaby.bikerental.rental.domain.model.RentalStatus;

public interface RentalLifecycleUseCase {

    Rental execute(RentalLifecycleCommand command);

    record RentalLifecycleCommand(
            Long rentalId,
            RentalStatus targetStatus,
            String operatorId
    ) {
    }
}