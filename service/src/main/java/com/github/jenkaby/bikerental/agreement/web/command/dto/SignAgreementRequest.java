package com.github.jenkaby.bikerental.agreement.web.command.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record SignAgreementRequest(
        @NotBlank
        String signaturePng,

        @NotNull
        @PositiveOrZero
        Long rentalVersion,

        @NotNull
        @Positive
        Long templateId,

        @NotBlank
        String operatorId
) {
}
