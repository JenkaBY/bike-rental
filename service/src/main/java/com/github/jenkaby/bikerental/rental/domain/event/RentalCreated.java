package com.github.jenkaby.bikerental.rental.domain.event;

import com.github.jenkaby.bikerental.shared.infrastructure.messaging.BikeRentalEvent;

import java.time.Instant;


public record RentalCreated(
        Long rentalId,
        Instant createdAt
) implements BikeRentalEvent {
}
