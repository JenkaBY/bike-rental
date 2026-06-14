package com.github.jenkaby.bikerental.tariff;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.time.LocalDateTime;

public record EquipmentCostItemV2(
        @NonNull Long equipmentId,
        @NonNull String equipmentType,
        @Nullable LocalDateTime returnAt) {

    public boolean isEstimate() {
        return returnAt() == null;
    }
}
