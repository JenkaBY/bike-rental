package com.github.jenkaby.bikerental.componenttest.transformer.finance;

import com.github.jenkaby.bikerental.componenttest.transformer.DataTableHelper;
import com.github.jenkaby.bikerental.componenttest.transformer.shared.Aliases;
import com.github.jenkaby.bikerental.finance.PaymentReceived;
import com.github.jenkaby.bikerental.finance.domain.model.PaymentType;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
import io.cucumber.java.DataTableType;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class PaymentReceivedTransformer {

    @DataTableType
    public PaymentReceived paymentReceived(Map<String, String> entry) {
        var paymentIdString = DataTableHelper.getStringOrNull(entry, "paymentId");
        UUID paymentId = paymentIdString != null ? Aliases.getPaymentId(paymentIdString) : null;

        var rentalId = DataTableHelper.toLong(entry, "rentalId");
        var amount = DataTableHelper.toBigDecimal(entry, "amount");
        var money = Money.of(amount);

        var paymentTypeString = DataTableHelper.getStringOrNull(entry, "type");
        var paymentType = paymentTypeString != null ? PaymentType.valueOf(paymentTypeString) : null;

        var receivedAt = Optional.ofNullable(DataTableHelper.toLocalDateTime(entry, "receivedAt")).orElse(LocalDateTime.now());

        return new PaymentReceived(paymentId, rentalId, money, paymentType, DataTableHelper.toInstant(receivedAt));
    }
}
