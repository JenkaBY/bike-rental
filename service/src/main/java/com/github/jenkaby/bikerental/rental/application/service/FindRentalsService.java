package com.github.jenkaby.bikerental.rental.application.service;

import com.github.jenkaby.bikerental.rental.application.usecase.FindRentalsUseCase;
import com.github.jenkaby.bikerental.rental.domain.model.Rental;
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
        // Priority: equipmentUid + status > customerId + status > customerId > status
        if (query.equipmentUid() != null && query.status() != null) {
            return repository.findByStatusAndEquipmentUid(query.status(), query.equipmentUid(), query.pageRequest());
        } else if (query.customerId() != null && query.status() != null) {
            return repository.findByStatusAndCustomerId(query.status(), query.customerId(), query.pageRequest());
        } else if (query.customerId() != null) {
            return repository.findByCustomerId(query.customerId(), query.pageRequest());
        } else {
            return repository.findByStatus(query.status(), query.pageRequest());
        }
    }
}
