package com.github.jenkaby.bikerental.rental.application.usecase;

import com.github.jenkaby.bikerental.rental.domain.model.Rental;
import com.github.jenkaby.bikerental.rental.domain.model.RentalStatus;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Page;
import com.github.jenkaby.bikerental.shared.domain.model.vo.PageRequest;

import org.jspecify.annotations.Nullable;

import java.time.LocalDate;
import java.util.UUID;

public interface FindRentalsUseCase {
    Page<Rental> execute(FindRentalsQuery query);

    record FindRentalsQuery(
            @Nullable RentalStatus status,
            @Nullable UUID customerId,
            @Nullable String equipmentUid,
            PageRequest pageRequest,
            @Nullable LocalDate from,
            @Nullable LocalDate to
    ) {
    }
}
