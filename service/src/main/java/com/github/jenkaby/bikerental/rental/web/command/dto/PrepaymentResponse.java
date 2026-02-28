package com.github.jenkaby.bikerental.rental.web.command.dto;

import com.github.jenkaby.bikerental.finance.PaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "Prepayment confirmation")
public record PrepaymentResponse(
        @Schema(description = "Payment UUID") UUID paymentId,
        @Schema(description = "Amount paid", example = "200.00") BigDecimal amount,
        @Schema(description = "Payment method") PaymentMethod paymentMethod,
        @Schema(description = "Receipt number", example = "RCP-20260228-001") String receiptNumber,
        @Schema(description = "Timestamp when payment was recorded") Instant createdAt
) {
}
