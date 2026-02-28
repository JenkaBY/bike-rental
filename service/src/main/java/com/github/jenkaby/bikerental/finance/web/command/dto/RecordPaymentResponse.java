package com.github.jenkaby.bikerental.finance.web.command.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Result of recording a payment")
public record RecordPaymentResponse(
        @Schema(description = "Payment UUID") UUID paymentId,
        @Schema(description = "Receipt number", example = "RCP-20260228-001") String receiptNumber
) {
}
