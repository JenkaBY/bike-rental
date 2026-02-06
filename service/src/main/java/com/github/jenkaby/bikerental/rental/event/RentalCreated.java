package com.github.jenkaby.bikerental.rental.event;

import com.github.jenkaby.bikerental.shared.domain.event.BikeRentalEvent;

import java.time.Instant;

public record RentalCreated(
        Long rentalId,
        Instant createdAt
) implements BikeRentalEvent {
}
