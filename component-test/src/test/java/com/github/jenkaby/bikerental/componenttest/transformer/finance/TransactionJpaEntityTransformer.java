package com.github.jenkaby.bikerental.componenttest.transformer.finance;

import com.github.jenkaby.bikerental.componenttest.transformer.DataTableHelper;
import com.github.jenkaby.bikerental.componenttest.transformer.shared.Aliases;
import com.github.jenkaby.bikerental.finance.PaymentMethod;
import com.github.jenkaby.bikerental.finance.domain.model.TransactionSourceType;
import com.github.jenkaby.bikerental.finance.domain.model.TransactionType;
import com.github.jenkaby.bikerental.finance.infrastructure.persistence.entity.TransactionJpaEntity;
import io.cucumber.java.DataTableType;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public class TransactionJpaEntityTransformer {

    @DataTableType
    public TransactionJpaEntity transform(Map<String, String> entry) {
        TransactionJpaEntity entity = new TransactionJpaEntity();

        var idString = DataTableHelper.getStringOrNull(entry, "id");
        entity.setId(idString == null ? null : UUID.fromString(idString));

        entity.setTransactionType(TransactionType.valueOf(DataTableHelper.getStringOrNull(entry, "type")));

        entity.setPaymentMethod(PaymentMethod.valueOf(DataTableHelper.getStringOrNull(entry, "paymentMethod")));
        entity.setAmount(DataTableHelper.toBigDecimal(entry, "amount"));

        entity.setCustomerId(Aliases.getCustomerId(entry.get("customerId")));
        entity.setOperatorId(Aliases.getOperatorId(DataTableHelper.getStringOrNull(entry, "operatorId")));

        var sourceType = DataTableHelper.getStringOrNull(entry, "sourceType");
        if (sourceType != null) {
            entity.setSourceType(TransactionSourceType.valueOf(sourceType));
        }

        entity.setSourceId(DataTableHelper.getStringOrNull(entry, "sourceId"));

        Instant recordedAt = DataTableHelper.parseLocalDateTimeToInstant(entry, "recordedAt");
        entity.setRecordedAt(recordedAt);

        UUID idempotencyKey = Aliases.getUuid(entry.get("idempotencyKey"));
        entity.setIdempotencyKey(idempotencyKey);

        return entity;
    }
}

