package com.github.jenkaby.bikerental.agreement.web.query.dto;

import org.jspecify.annotations.NonNull;

import java.time.Instant;

public record RentalAgreementResponse(
        @NonNull
        Long templateId,
        @NonNull
        Integer versionNumber,
        @NonNull
        String title,
        @NonNull
        String content,
        @NonNull
        Instant templateActivatedAt) {
}
