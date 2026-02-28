package com.github.jenkaby.bikerental.finance.web.command.dto;

import com.github.jenkaby.bikerental.finance.PaymentMethod;
import com.github.jenkaby.bikerental.finance.domain.model.PaymentType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Schema(description = "Request body for recording a payment")
public record RecordPaymentRequest(
        @Schema(description = "Rental ID the payment is for", example = "42") Long rentalId,
        @Schema(description = "Payment amount", example = "500.00") @NotNull @DecimalMin(value = "0.01") BigDecimal amount,
        @Schema(description = "Payment type (e.g. PREPAYMENT, FINAL)") @NotNull PaymentType paymentType,
        @Schema(description = "Payment method (e.g. CASH, CARD)") @NotNull PaymentMethod paymentMethod,
        @Schema(description = "Operator identifier", example = "operator-1") String operatorId
) {
}
