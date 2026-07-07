package com.github.jenkaby.bikerental.agreement.web.query.dto;

public record RentalAgreementResponse(
        Long templateId,
        Integer versionNumber,
        String title,
        String content) {
}
