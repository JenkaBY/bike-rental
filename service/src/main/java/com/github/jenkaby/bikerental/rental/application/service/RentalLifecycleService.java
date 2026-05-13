package com.github.jenkaby.bikerental.rental.application.service;

import com.github.jenkaby.bikerental.rental.application.usecase.ActivateRentalUseCase;
import com.github.jenkaby.bikerental.rental.application.usecase.CancelRentalUseCase;
import com.github.jenkaby.bikerental.rental.application.usecase.RentalLifecycleUseCase;
import com.github.jenkaby.bikerental.rental.domain.model.Rental;
import org.springframework.stereotype.Service;

@Service
class RentalLifecycleService implements RentalLifecycleUseCase {

    private final ActivateRentalUseCase activateRentalUseCase;
    private final CancelRentalUseCase cancelRentalUseCase;

    RentalLifecycleService(ActivateRentalUseCase activateRentalUseCase,
                           CancelRentalUseCase cancelRentalUseCase) {
        this.activateRentalUseCase = activateRentalUseCase;
        this.cancelRentalUseCase = cancelRentalUseCase;
    }

    @Override
    public Rental execute(RentalLifecycleCommand command) {
        return switch (command.targetStatus()) {
            case ACTIVE -> activateRentalUseCase.execute(
                    new ActivateRentalUseCase.ActivateCommand(command.rentalId(), command.operatorId()));
            case CANCELLED -> cancelRentalUseCase.execute(
                    new CancelRentalUseCase.CancelCommand(command.rentalId(), command.operatorId()));
            default -> throw new IllegalArgumentException(
                    "Unsupported lifecycle target status: " + command.targetStatus());
        };
    }
}