package com.github.jenkaby.bikerental.rental;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

public record RentalSigningSnapshot(
        Long rentalId,
        Long version,
        UUID customerId,
        Duration plannedDuration,
        BigDecimal estimatedCost,
        Integer discountPercent,
        BigDecimal specialPrice,
        List<EquipmentItem> equipments
) {

    public record EquipmentItem(
            Long equipmentId,
            String equipmentUid,
            String equipmentTypeSlug,
            BigDecimal estimatedCost
    ) {
    }
}
