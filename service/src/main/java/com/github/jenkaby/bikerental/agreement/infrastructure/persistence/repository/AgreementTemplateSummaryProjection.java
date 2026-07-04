package com.github.jenkaby.bikerental.agreement.infrastructure.persistence.repository;

import com.github.jenkaby.bikerental.agreement.domain.model.AgreementTemplateStatus;

import java.time.Instant;

public interface AgreementTemplateSummaryProjection {

    Long getId();

    Integer getVersionNumber();

    String getTitle();

    AgreementTemplateStatus getStatus();

    Instant getCreatedAt();

    Instant getActivatedAt();

    Instant getDeactivatedAt();
}
