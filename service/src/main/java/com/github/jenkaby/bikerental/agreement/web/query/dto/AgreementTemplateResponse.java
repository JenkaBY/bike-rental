package com.github.jenkaby.bikerental.agreement.web.query.dto;

import com.github.jenkaby.bikerental.agreement.domain.model.AgreementTemplateStatus;

import java.time.Instant;

public record AgreementTemplateResponse(
        Long id,
        Integer versionNumber,
        String title,
        String content,
        AgreementTemplateStatus status,
        Instant createdAt,
        Instant activatedAt,
        Instant deactivatedAt
) {
}
