package com.github.jenkaby.bikerental.tariff;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.time.Duration;
import java.time.LocalDateTime;

public record EquipmentCostItemV2(
        @NonNull Long equipmentId,
        @NonNull String equipmentType,
        @Nullable LocalDateTime startAt,
        @Nullable Duration plannedDuration,
        @Nullable LocalDateTime returnAt) {

    public boolean isEstimate() {
        return returnAt() == null;
    }

    public LocalDateTime resolveStartAt(LocalDateTime commandStartAt) {
        return startAt != null ? startAt : commandStartAt;
    }

    public Duration resolvePlannedDuration(Duration commandPlannedDuration) {
        return plannedDuration != null ? plannedDuration : commandPlannedDuration;
    }
}
