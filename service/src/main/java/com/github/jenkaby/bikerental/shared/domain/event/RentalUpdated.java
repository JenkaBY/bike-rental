package com.github.jenkaby.bikerental.shared.domain.event;

import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.UUID;

public record RentalUpdated(
        Long rentalId,
        UUID customerId,
        @NonNull RentalState previousState,
        @NonNull RentalState currentState
) implements BikeRentalEvent {

    public record RentalState(@NonNull String rentalStatus, @NonNull List<Long> equipmentIds) {
    }
}
