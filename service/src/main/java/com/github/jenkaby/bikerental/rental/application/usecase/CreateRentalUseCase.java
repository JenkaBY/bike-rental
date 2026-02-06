package com.github.jenkaby.bikerental.rental.application.usecase;

import com.github.jenkaby.bikerental.rental.domain.model.Rental;


public interface CreateRentalUseCase {


    Rental execute(CreateRentalCommand command);


    record CreateRentalCommand() {
    }
}
