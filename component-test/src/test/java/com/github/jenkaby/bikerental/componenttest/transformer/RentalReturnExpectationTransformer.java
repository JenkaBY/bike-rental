package com.github.jenkaby.bikerental.componenttest.transformer;

import com.github.jenkaby.bikerental.componenttest.model.RentalReturnExpectation;
import com.github.jenkaby.bikerental.finance.PaymentMethod;
import io.cucumber.java.DataTableType;

import java.util.Map;

public class RentalReturnExpectationTransformer {

    @DataTableType
    public RentalReturnExpectation transform(Map<String, String> entry) {
        var additionalPayment = DataTableHelper.toBigDecimal(entry, "additionalPayment");
        var paymentAmount = DataTableHelper.toBigDecimal(entry, "paymentAmount");

        String paymentMethodStr = DataTableHelper.getStringOrNull(entry, "paymentMethod");
        var paymentMethod = paymentMethodStr != null ? PaymentMethod.valueOf(paymentMethodStr) : null;
        var receiptNumber = DataTableHelper.getStringOrNull(entry, "receiptNumber");

        return new RentalReturnExpectation(
                additionalPayment,
                paymentAmount,
                paymentMethod,
                receiptNumber
        );
    }
}
