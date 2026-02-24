package com.github.jenkaby.bikerental.rental.application.usecase;

import com.github.jenkaby.bikerental.rental.domain.model.Rental;
import com.github.jenkaby.bikerental.rental.domain.model.RentalStatus;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Page;
import com.github.jenkaby.bikerental.shared.domain.model.vo.PageRequest;

import java.util.UUID;

public interface FindRentalsUseCase {
    Page<Rental> execute(FindRentalsQuery query);

    record FindRentalsQuery(
            RentalStatus status,
            UUID customerId,
            PageRequest pageRequest
    ) {
    }
}
