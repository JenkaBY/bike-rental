package com.github.jenkaby.bikerental.componenttest.transformer;

import com.github.jenkaby.bikerental.componenttest.transformer.shared.Aliases;
import com.github.jenkaby.bikerental.finance.web.query.dto.PaymentResponse;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
import io.cucumber.java.DataTableType;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

public class PaymentResponseTransformer {

    @DataTableType
    public PaymentResponse paymentResponse(Map<String, String> entry) {
        var idString = DataTableHelper.getStringOrNull(entry, "id");
        var id = idString != null ? Aliases.getPaymentId(idString) : null;

        var rentalId = DataTableHelper.toLong(entry, "rentalId");
        var amount = DataTableHelper.toBigDecimal(entry, "amount");
        var paymentType = DataTableHelper.getStringOrNull(entry, "type");
        var paymentMethod = DataTableHelper.getStringOrNull(entry, "method");
        var createdAt = Optional.ofNullable(DataTableHelper.toInstant(entry, "createdAt")).orElse(Instant.now());
        var operatorId = Aliases.getOperatorId(entry.get("operator"));
        var receiptNumber = DataTableHelper.getStringOrNull(entry, "receipt");

        var money = Money.of(amount);

        return new PaymentResponse(id, rentalId, money, paymentType, paymentMethod, createdAt, operatorId, receiptNumber);
    }
}
