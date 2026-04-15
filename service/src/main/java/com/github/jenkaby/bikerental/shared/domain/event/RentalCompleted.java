package com.github.jenkaby.bikerental.shared.domain.event;

import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
import org.jspecify.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.List;

public record RentalCompleted(
        Long rentalId,
        List<Long> equipmentIds,
        List<Long> returnedEquipmentIds,
        LocalDateTime returnTime,
        @Nullable Money totalCost
) implements BikeRentalEvent {
}
