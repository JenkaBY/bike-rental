package com.github.jenkaby.bikerental.componenttest.transformer;

import com.github.jenkaby.bikerental.componenttest.transformer.shared.Aliases;
import com.github.jenkaby.bikerental.finance.PaymentMethod;
import com.github.jenkaby.bikerental.rental.web.command.dto.PrepaymentResponse;
import io.cucumber.java.DataTableType;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class PrepaymentResponseTransformer {

    @DataTableType
    public PrepaymentResponse prepaymentResponse(Map<String, String> entry) {
        var paymentIdString = DataTableHelper.getStringOrNull(entry, "paymentId");
        UUID paymentId = Optional.ofNullable(paymentIdString)
                .map(Aliases::getPaymentId)
                .orElse(null);

        var amount = DataTableHelper.toBigDecimal(entry, "amount");
        var paymentMethodString = DataTableHelper.getStringOrNull(entry, "paymentMethod");
        PaymentMethod paymentMethod = Optional.ofNullable(paymentMethodString)
                .map(PaymentMethod::valueOf)
                .orElse(null);

        var receiptNumber = DataTableHelper.getStringOrNull(entry, "receiptNumber");
        var createdAt = Optional.ofNullable(DataTableHelper.toInstant(entry, "createdAt"))
                .orElse(Instant.now());

        return new PrepaymentResponse(paymentId, amount, paymentMethod, receiptNumber, createdAt);
    }
}
