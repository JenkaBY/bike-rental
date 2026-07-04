package com.github.jenkaby.bikerental.agreement.web.query.dto;

import java.time.Instant;

public record SignatureSummaryResponse(
        Long signatureId,
        Long templateId,
        Integer templateVersionNumber,
        Instant signedAt) {
}
