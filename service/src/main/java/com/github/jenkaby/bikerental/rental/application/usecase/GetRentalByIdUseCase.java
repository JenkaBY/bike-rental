package com.github.jenkaby.bikerental.rental.application.usecase;

import com.github.jenkaby.bikerental.rental.domain.model.Rental;


public interface GetRentalByIdUseCase {

    Rental execute(Long id);
}
