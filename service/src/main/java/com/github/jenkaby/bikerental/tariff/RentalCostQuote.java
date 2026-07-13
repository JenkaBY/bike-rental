package com.github.jenkaby.bikerental.tariff;

import org.jspecify.annotations.NonNull;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record RentalCostQuote(
        @NonNull UUID quoteId,
        @NonNull Instant quotedAt,
        @NonNull Instant expiresAt,
        @NonNull QuoteStatus status,
        @NonNull RentalCostCalculationV2Command inputs,
        @NonNull RentalCostCalculationResult result
) {

    public boolean isExpired(Instant now) {
        return !now.isBefore(expiresAt);
    }

    public boolean isConsumed() {
        return status == QuoteStatus.CONSUMED;
    }

    public List<Long> equipmentIds() {
        return inputs.equipments().stream()
                .map(EquipmentCostItemV2::equipmentId)
                .toList();
    }

    public LocalDateTime resolveReturnTime() {
        return inputs.equipments().stream()
                .map(EquipmentCostItemV2::returnAt)
                .filter(Objects::nonNull)
                .max(Comparator.naturalOrder())
                .orElseThrow(() -> new IllegalStateException("Quote has no concrete return timestamps"));
    }
}
