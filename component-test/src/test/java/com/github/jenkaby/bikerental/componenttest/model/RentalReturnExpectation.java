package com.github.jenkaby.bikerental.componenttest.model;

import com.github.jenkaby.bikerental.finance.PaymentMethod;

import java.math.BigDecimal;

public record RentalReturnExpectation(
        BigDecimal additionalPayment,
        BigDecimal paymentAmount,
        PaymentMethod paymentMethod,
        String receiptNumber
) {
}
