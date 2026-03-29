package com.github.jenkaby.bikerental.componenttest.transformer.finance;

import com.github.jenkaby.bikerental.componenttest.transformer.shared.Aliases;
import com.github.jenkaby.bikerental.finance.PaymentMethod;
import com.github.jenkaby.bikerental.finance.web.command.dto.RecordDepositRequest;
import io.cucumber.java.DataTableType;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

public class DepositRequestTransformer {

	@DataTableType
	public RecordDepositRequest transform(Map<String, String> entry) {
		UUID idempotencyKey = Aliases.getUuid(entry.get("idempotencyKey"));
		UUID customerId = Aliases.getCustomerId(entry.get("customerId"));
		BigDecimal amount = new BigDecimal(entry.get("amount"));
		PaymentMethod paymentMethod = PaymentMethod.valueOf(entry.get("paymentMethod"));
		String operatorId = Aliases.getOperatorId(entry.get("operatorId"));
		return new RecordDepositRequest(idempotencyKey, customerId, amount, paymentMethod, operatorId);
	}
}
