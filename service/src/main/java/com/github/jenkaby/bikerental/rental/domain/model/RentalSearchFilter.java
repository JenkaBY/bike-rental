package com.github.jenkaby.bikerental.rental.domain.model;

import org.jspecify.annotations.Nullable;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record RentalSearchFilter(
        List<RentalStatus> statuses,
        @Nullable UUID customerId,
        @Nullable String equipmentUid,
        @Nullable LocalDate from,
        @Nullable LocalDate to
) {

    public RentalSearchFilter {
        statuses = statuses == null ? List.of() : List.copyOf(statuses);
    }
}
