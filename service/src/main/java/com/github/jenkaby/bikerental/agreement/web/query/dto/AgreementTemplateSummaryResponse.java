package com.github.jenkaby.bikerental.agreement.web.query.dto;

import com.github.jenkaby.bikerental.agreement.domain.model.AgreementTemplateStatus;

import java.time.Instant;

public record AgreementTemplateSummaryResponse(
        Long id,
        Integer versionNumber,
        String title,
        AgreementTemplateStatus status,
        Instant createdAt,
        Instant activatedAt,
        Instant deactivatedAt
) {
}
