package com.github.jenkaby.bikerental.rental.application.service;

import com.github.jenkaby.bikerental.rental.application.usecase.CreateRentalUseCase;
import com.github.jenkaby.bikerental.rental.domain.model.Rental;
import com.github.jenkaby.bikerental.rental.domain.repository.RentalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
class CreateRentalService implements CreateRentalUseCase {

    private final RentalRepository repository;

    CreateRentalService(RentalRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public Rental execute(CreateRentalCommand command) {
        Rental rental = Rental.createDraft();
        return repository.save(rental);
    }
}
