package com.github.jenkaby.bikerental.componenttest.transformer.finance;

import com.github.jenkaby.bikerental.componenttest.transformer.DataTableHelper;
import com.github.jenkaby.bikerental.componenttest.transformer.shared.Aliases;
import com.github.jenkaby.bikerental.finance.PaymentMethod;
import com.github.jenkaby.bikerental.finance.domain.model.TransactionSourceType;
import com.github.jenkaby.bikerental.finance.web.command.dto.RecordWithdrawalRequest;
import io.cucumber.java.DataTableType;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

public class WithdrawalRequestTransformer {

    @DataTableType
    public RecordWithdrawalRequest transform(Map<String, String> entry) {
        UUID idempotencyKey = Aliases.getUuid(entry.get("idempotencyKey"));
        UUID customerId = Aliases.getCustomerId(entry.get("customerId"));
        BigDecimal amount = new BigDecimal(entry.get("amount"));
        PaymentMethod payoutMethod = PaymentMethod.valueOf(entry.get("paymentMethod"));
        String operatorId = Aliases.getOperatorId(entry.get("operatorId"));
        String sourceString = DataTableHelper.getStringOrNull(entry, "source");
        TransactionSourceType source = sourceString == null ? null : TransactionSourceType.valueOf(sourceString);
        String sourceIdString = DataTableHelper.getStringOrNull(entry, "sourceId");
        String sourceId = sourceIdString == null ? null : Aliases.getValueOrDefault(sourceIdString);
        return new RecordWithdrawalRequest(idempotencyKey, customerId, amount, payoutMethod, operatorId, source, sourceId);
    }
}
