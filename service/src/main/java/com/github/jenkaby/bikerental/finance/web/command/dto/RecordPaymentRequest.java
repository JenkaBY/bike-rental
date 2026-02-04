package com.github.jenkaby.bikerental.finance.web.command.dto;

import com.github.jenkaby.bikerental.finance.domain.model.PaymentMethod;
import com.github.jenkaby.bikerental.finance.domain.model.PaymentType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record RecordPaymentRequest(
        Long rentalId,
        @NotNull @DecimalMin(value = "0.01") BigDecimal amount,
        @NotNull PaymentType paymentType,
        @NotNull PaymentMethod paymentMethod,
        String operatorId
) {
}
