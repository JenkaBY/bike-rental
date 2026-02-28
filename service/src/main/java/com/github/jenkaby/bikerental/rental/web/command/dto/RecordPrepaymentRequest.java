package com.github.jenkaby.bikerental.rental.web.command.dto;

import com.github.jenkaby.bikerental.finance.PaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Schema(description = "Request body for recording a prepayment against a rental")
public record RecordPrepaymentRequest(
        @Schema(description = "Prepayment amount", example = "200.00")
        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be positive")
        BigDecimal amount,

        @Schema(description = "Payment method (e.g. CASH, CARD)")
        @NotNull(message = "Payment method is required")
        PaymentMethod paymentMethod,

        @Schema(description = "Operator identifier", example = "operator-1")
        @NotBlank(message = "Operator is required")
        String operatorId
) {
}
