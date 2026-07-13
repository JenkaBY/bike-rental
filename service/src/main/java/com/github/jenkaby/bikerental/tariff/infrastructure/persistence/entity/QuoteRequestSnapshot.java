package com.github.jenkaby.bikerental.tariff.infrastructure.persistence.entity;

import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record QuoteRequestSnapshot(
        List<Item> equipments,
        Integer plannedDurationMinutes,
        @Nullable Integer discountPercent,
        @Nullable Long specialTariffId,
        @Nullable BigDecimal specialPrice,
        LocalDateTime startAt
) {

    public record Item(
            Long equipmentId,
            String equipmentType,
            @Nullable LocalDateTime startAt,
            @Nullable Integer plannedDurationMinutes,
            @Nullable LocalDateTime returnAt
    ) {
    }
}
