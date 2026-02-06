package com.github.jenkaby.bikerental.rental.application.service;

import com.github.jenkaby.bikerental.rental.application.usecase.GetRentalByIdUseCase;
import com.github.jenkaby.bikerental.rental.domain.model.Rental;
import com.github.jenkaby.bikerental.rental.domain.repository.RentalRepository;
import com.github.jenkaby.bikerental.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;


@Service
class GetRentalByIdService implements GetRentalByIdUseCase {

    private final RentalRepository repository;

    GetRentalByIdService(RentalRepository repository) {
        this.repository = repository;
    }

    @Override
    public Rental execute(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(Rental.class, id));
    }
}
