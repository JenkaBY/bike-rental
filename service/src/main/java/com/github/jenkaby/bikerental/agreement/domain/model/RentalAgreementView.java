package com.github.jenkaby.bikerental.agreement.domain.model;

import java.time.Instant;

public record RentalAgreementView(
        Long templateId,
        Integer versionNumber,
        String title,
        String content,
        Instant templateActivatedAt) {
}
