package com.github.jenkaby.bikerental.rental.application.service;

import com.github.jenkaby.bikerental.rental.application.usecase.FindRentalsUseCase;
import com.github.jenkaby.bikerental.rental.domain.exception.InvalidDateRangeException;
import com.github.jenkaby.bikerental.rental.domain.model.Rental;
import com.github.jenkaby.bikerental.rental.domain.model.RentalSearchFilter;
import com.github.jenkaby.bikerental.rental.domain.repository.RentalRepository;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Page;
import org.springframework.stereotype.Service;

@Service
class FindRentalsService implements FindRentalsUseCase {

    private final RentalRepository repository;

    FindRentalsService(RentalRepository repository) {
        this.repository = repository;
    }

    @Override
    public Page<Rental> execute(FindRentalsQuery query) {
        if (query.from() != null && query.to() != null && query.from().isAfter(query.to())) {
            throw new InvalidDateRangeException(query.from(), query.to());
        }
        var filter = new RentalSearchFilter(
                query.status(),
                query.customerId(),
                query.equipmentUid(),
                query.from(),
                query.to()
        );
        return repository.findAll(filter, query.pageRequest());
    }
}
