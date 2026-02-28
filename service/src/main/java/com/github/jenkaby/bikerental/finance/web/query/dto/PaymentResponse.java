package com.github.jenkaby.bikerental.finance.web.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "Payment record")
public record PaymentResponse(
        @Schema(description = "Payment UUID") UUID id,
        @Schema(description = "Rental ID", example = "42") Long rentalId,
        @Schema(description = "Amount", example = "500.00") BigDecimal amount,
        @Schema(description = "Payment type", example = "PREPAYMENT") String paymentType,
        @Schema(description = "Payment method", example = "CASH") String paymentMethod,
        @Schema(description = "Timestamp when payment was recorded") Instant createdAt,
        @Schema(description = "Operator identifier", example = "operator-1") String operatorId,
        @Schema(description = "Receipt number", example = "RCP-20260228-001") String receiptNumber
) {
}
