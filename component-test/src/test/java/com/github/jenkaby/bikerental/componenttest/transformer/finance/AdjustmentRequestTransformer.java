package com.github.jenkaby.bikerental.componenttest.transformer.finance;

import com.github.jenkaby.bikerental.componenttest.transformer.DataTableHelper;
import com.github.jenkaby.bikerental.componenttest.transformer.shared.Aliases;
import com.github.jenkaby.bikerental.finance.web.command.dto.AdjustmentRequest;
import io.cucumber.java.DataTableType;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

public class AdjustmentRequestTransformer {

    @DataTableType
    public AdjustmentRequest transform(Map<String, String> entry) {
        UUID customerId = Aliases.getCustomerId(entry.get("customerId"));
        BigDecimal amount = DataTableHelper.toBigDecimal(entry, "amount");
        String reason = entry.get("reason");
        String operatorId = Aliases.getOperatorId(entry.get("operatorId"));
        UUID idempotencyKey = Aliases.getUuid(entry.get("idempotencyKey"));
        return new AdjustmentRequest(customerId, amount, reason, operatorId, idempotencyKey);
    }
}
