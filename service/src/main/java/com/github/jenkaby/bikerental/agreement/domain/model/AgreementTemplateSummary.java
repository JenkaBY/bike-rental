package com.github.jenkaby.bikerental.agreement.domain.model;

import java.time.Instant;

public record AgreementTemplateSummary(
        Long id,
        Integer versionNumber,
        String title,
        AgreementTemplateStatus status,
        Instant createdAt,
        Instant activatedAt,
        Instant deactivatedAt
) {
}
