package com.github.jenkaby.bikerental.componenttest.transformer;

import com.github.jenkaby.bikerental.componenttest.transformer.shared.Aliases;
import com.github.jenkaby.bikerental.finance.PaymentMethod;
import com.github.jenkaby.bikerental.rental.web.command.dto.RecordPrepaymentRequest;
import io.cucumber.java.DataTableType;

import java.math.BigDecimal;
import java.util.Map;

public class RecordPrepaymentRequestTransformer {

    @DataTableType
    public RecordPrepaymentRequest recordPrepaymentRequest(Map<String, String> entry) {
        BigDecimal amount = DataTableHelper.toBigDecimal(entry, "amount");

        PaymentMethod paymentMethod = PaymentMethod.valueOf(DataTableHelper.getStringOrNull(entry, "method"));
        String operatorId = Aliases.getOperatorId(entry.get("operator"));

        return new RecordPrepaymentRequest(amount, paymentMethod, operatorId);
    }
}
