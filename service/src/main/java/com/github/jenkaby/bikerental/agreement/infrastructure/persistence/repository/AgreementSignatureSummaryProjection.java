package com.github.jenkaby.bikerental.agreement.infrastructure.persistence.repository;

import java.time.Instant;

public interface AgreementSignatureSummaryProjection {

    Long getId();

    Long getTemplateId();

    Integer getTemplateVersionNumber();

    Instant getSignedAt();
}
