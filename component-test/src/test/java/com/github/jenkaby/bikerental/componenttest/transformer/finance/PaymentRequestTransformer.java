package com.github.jenkaby.bikerental.componenttest.transformer.finance;

import com.github.jenkaby.bikerental.componenttest.transformer.DataTableHelper;
import com.github.jenkaby.bikerental.componenttest.transformer.shared.Aliases;
import com.github.jenkaby.bikerental.finance.PaymentMethod;
import com.github.jenkaby.bikerental.finance.domain.model.PaymentType;
import com.github.jenkaby.bikerental.finance.web.command.dto.RecordPaymentRequest;
import io.cucumber.java.DataTableType;

import java.math.BigDecimal;
import java.util.Map;

public class PaymentRequestTransformer {

    @DataTableType
    public RecordPaymentRequest paymentRequest(Map<String, String> entry) {
        Long rentalId = DataTableHelper.toLong(entry, "rentalId");
        BigDecimal amount = DataTableHelper.toBigDecimal(entry, "amount");

        String paymentTypeStr = DataTableHelper.getStringOrNull(entry, "type");
        PaymentType paymentType = paymentTypeStr != null ? PaymentType.valueOf(paymentTypeStr) : null;

        String paymentMethodStr = DataTableHelper.getStringOrNull(entry, "method");
        PaymentMethod paymentMethod = paymentMethodStr != null ? PaymentMethod.valueOf(paymentMethodStr) : null;

        String operatorId = Aliases.getOperatorId(entry.get("operator"));

        return new RecordPaymentRequest(rentalId, amount, paymentType, paymentMethod, operatorId);
    }
}
