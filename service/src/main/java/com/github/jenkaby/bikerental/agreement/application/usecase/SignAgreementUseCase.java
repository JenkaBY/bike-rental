package com.github.jenkaby.bikerental.agreement.application.usecase;

import java.time.Instant;

public interface SignAgreementUseCase {

    SignAgreementResult execute(SignAgreementCommand command);

    record SignAgreementCommand(
            Long rentalId,
            String signaturePngBase64,
            Long rentalVersion,
            Long templateId,
            String operatorId,
            String ipAddress,
            String userAgent) {
    }

    record SignAgreementResult(Long signatureId, Instant signedAt) {
    }
}
