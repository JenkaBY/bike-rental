package com.github.jenkaby.bikerental.agreement.infrastructure.persistence.projection;

import java.time.Instant;

public interface AgreementSignatureSummaryProjection {

    Long getId();

    Long getTemplateId();

    Integer getTemplateVersionNumber();

    Instant getSignedAt();
}
