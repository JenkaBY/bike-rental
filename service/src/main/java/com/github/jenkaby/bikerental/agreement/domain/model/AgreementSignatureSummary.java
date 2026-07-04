package com.github.jenkaby.bikerental.agreement.domain.model;

import java.time.Instant;

public record AgreementSignatureSummary(
        Long id,
        Long templateId,
        Integer templateVersionNumber,
        Instant signedAt) {
}
