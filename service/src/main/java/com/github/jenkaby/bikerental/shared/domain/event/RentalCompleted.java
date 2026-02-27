package com.github.jenkaby.bikerental.shared.domain.event;

import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;

import java.time.LocalDateTime;

public record RentalCompleted(
        Long rentalId,
        Long equipmentId,
        LocalDateTime returnTime,
        Money finalCost
) implements BikeRentalEvent {
}
