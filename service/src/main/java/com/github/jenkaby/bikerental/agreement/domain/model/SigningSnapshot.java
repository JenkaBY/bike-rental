package com.github.jenkaby.bikerental.agreement.domain.model;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public record SigningSnapshot(
        Customer customer,
        Rental rental,
        Template template) {

    public record Customer(String firstName, String lastName, String phone) {
    }

    public record Rental(Long rentalId,
                         Long rentalVersion,
                         Duration plannedDuration,
                         LocalDateTime startedAt,
                         List<EquipmentLine> equipments,
                         BigDecimal estimatedTotal,
                         Integer discountPercent,
                         BigDecimal specialPrice) {
    }

    public record EquipmentLine(String uid, String name, BigDecimal estimatedCost) {
    }

    public record Template(Long templateId, Integer versionNumber, String contentSha256) {
    }
}
