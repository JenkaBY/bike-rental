package com.github.jenkaby.bikerental.shared.domain.event;

import java.util.List;
import java.util.UUID;

public record RentalCancelled(
        Long rentalId,
        UUID customerId,
        List<Long> equipmentIds
) implements BikeRentalEvent {
}