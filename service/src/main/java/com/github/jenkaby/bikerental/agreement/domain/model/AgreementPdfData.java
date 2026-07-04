package com.github.jenkaby.bikerental.agreement.domain.model;

import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public record AgreementPdfData(
        String title,
        String content,
        CustomerData customer,
        RentalData rental,
        @Nullable byte[] signaturePng) {

    public record CustomerData(String firstName, String lastName, String phone) {
    }

    public record RentalData(Long rentalId,
                             LocalDateTime startedAt,
                             Duration plannedDuration,
                             List<EquipmentLine> equipments) {
    }

    public record EquipmentLine(String uid, String name, BigDecimal estimatedCost) {
    }
}
