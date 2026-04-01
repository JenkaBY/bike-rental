package com.github.jenkaby.bikerental.finance.web.command.dto;

import com.github.jenkaby.bikerental.finance.PaymentMethod;
import com.github.jenkaby.bikerental.shared.web.support.MoneyAmount;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Request body for recording a fund withdrawal")
public record RecordWithdrawalRequest(
        @Schema(description = "Client-generated UUID sent with every request to ensure exactly-once submission",
                example = "018e2cc3-0000-7000-8000-000000000099")
        @NotNull UUID idempotencyKey,

        @Schema(description = "Customer UUID", example = "018e2cc3-0000-7000-8000-000000000001")
        @NotNull UUID customerId,

        @Schema(description = "Withdrawal amount", example = "30.00")
        @NotNull @DecimalMin(value = "0.01") @MoneyAmount BigDecimal amount,

        @Schema(description = "Payout method (CASH, CARD_TERMINAL, BANK_TRANSFER)")
        @NotNull PaymentMethod payoutMethod,

        @Schema(description = "Operator identifier", example = "operator-1")
        @NotBlank String operatorId
) {
}
