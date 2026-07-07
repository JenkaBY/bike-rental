package com.github.jenkaby.bikerental.agreement.domain.model;

public record RentalAgreementView(
        Long templateId,
        Integer versionNumber,
        String title,
        String content) {
}
