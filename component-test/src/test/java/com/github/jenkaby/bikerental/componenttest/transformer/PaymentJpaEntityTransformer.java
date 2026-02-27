package com.github.jenkaby.bikerental.componenttest.transformer;

import com.github.jenkaby.bikerental.componenttest.transformer.shared.Aliases;
import com.github.jenkaby.bikerental.finance.infrastructure.persistence.entity.PaymentJpaEntity;
import io.cucumber.java.DataTableType;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

public class PaymentJpaEntityTransformer {

    @DataTableType
    public PaymentJpaEntity paymentJpaEntity(Map<String, String> entry) {
        var id = Optional.ofNullable(entry.get("id")).map(Aliases::getPaymentId).orElse(null);
        var rentalId = DataTableHelper.toLong(entry, "rentalId");
        var amount = DataTableHelper.toBigDecimal(entry, "amount");
        var paymentType = DataTableHelper.getStringOrNull(entry, "type");
        var paymentMethod = DataTableHelper.getStringOrNull(entry, "method");
        var createdAt = Optional.ofNullable(DataTableHelper.toInstant(entry, "createdAt")).orElse(Instant.now());
        var operatorId = Aliases.getValue(entry.get("operator"));
        var receiptNumber = DataTableHelper.getStringOrNull(entry, "receipt");

        return PaymentJpaEntity.builder()
                .id(id)
                .rentalId(rentalId)
                .amount(amount)
                .paymentType(paymentType)
                .paymentMethod(paymentMethod)
                .createdAt(createdAt)
                .receiptNumber(receiptNumber)
                .operatorId(operatorId)
                .build();
    }
}
