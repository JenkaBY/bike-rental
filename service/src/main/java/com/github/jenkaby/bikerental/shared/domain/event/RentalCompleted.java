package com.github.jenkaby.bikerental.shared.domain.event;

import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;

import java.time.LocalDateTime;
import java.util.List;

public record RentalCompleted(
        Long rentalId,
        List<Long> equipmentIds,
        LocalDateTime returnTime,
        Money finalCost
) implements BikeRentalEvent {
}
