package com.github.jenkaby.bikerental.rental.domain.model.vo;

import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
import org.jspecify.annotations.Nullable;

public record EquipmentCostResult(
        Long equipmentId,
        Long tariffId,
        Money estimatedCost,
        @Nullable RentalEquipmentCostBreakdown breakdown
) {

    public static EquipmentCostResult withoutBreakdown(Long equipmentId, Long tariffId, Money cost) {
        return new EquipmentCostResult(equipmentId, tariffId, cost, null);
    }
}