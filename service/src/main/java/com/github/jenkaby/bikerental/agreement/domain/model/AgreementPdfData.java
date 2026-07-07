package com.github.jenkaby.bikerental.agreement.domain.model;

import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

public record AgreementPdfData(
        AgreementTemplate template,
        CustomerData customer,
        RentalData rental,
        byte @Nullable [] signaturePng) {

    public record CustomerData(String firstName, String lastName, String phone) {
    }

    public record RentalData(Long rentalId,
                             Instant startedAt,
                             Duration plannedDuration,
                             List<EquipmentLine> equipments,
                             BigDecimal estimatedTotal,
                             @Nullable Integer discountPercent,
                             @Nullable BigDecimal specialPrice) {
    }

    public record EquipmentLine(String uid, String name, BigDecimal estimatedCost) {
    }
}
