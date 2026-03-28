package com.github.jenkaby.bikerental.componenttest.transformer.finance;

import com.github.jenkaby.bikerental.componenttest.transformer.DataTableHelper;
import com.github.jenkaby.bikerental.componenttest.transformer.shared.Aliases;
import com.github.jenkaby.bikerental.finance.PaymentMethod;
import com.github.jenkaby.bikerental.finance.domain.model.EntryDirection;
import com.github.jenkaby.bikerental.finance.domain.model.LedgerType;
import com.github.jenkaby.bikerental.finance.domain.model.TransactionSourceType;
import com.github.jenkaby.bikerental.finance.domain.model.TransactionType;
import com.github.jenkaby.bikerental.finance.infrastructure.persistence.entity.TransactionJpaEntity;
import com.github.jenkaby.bikerental.finance.infrastructure.persistence.entity.TransactionRecordJpaEntity;
import com.github.jenkaby.bikerental.rental.web.command.dto.RecordPrepaymentRequest;
import io.cucumber.java.DataTableType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public class RecordPrepaymentRequestTransformer {

    @DataTableType
    public RecordPrepaymentRequest recordPrepaymentRequest(Map<String, String> entry) {
        BigDecimal amount = DataTableHelper.toBigDecimal(entry, "amount");

        PaymentMethod paymentMethod = PaymentMethod.valueOf(DataTableHelper.getStringOrNull(entry, "method"));
        String operatorId = Aliases.getOperatorId(entry.get("operator"));

        return new RecordPrepaymentRequest(amount, paymentMethod, operatorId);
    }

    public static class TransactionJpaEntityTransformer {

        @DataTableType
        public TransactionJpaEntity transform(Map<String, String> entry) {
            TransactionJpaEntity entity = new TransactionJpaEntity();

            String idStr = DataTableHelper.getStringOrNull(entry, "id");
            entity.setId(idStr == null ? null : UUID.fromString(idStr));

            String type = DataTableHelper.getStringOrNull(entry, "type");
            if (type == null) {
                type = DataTableHelper.getStringOrNull(entry, "transactionType");
            }
            if (type != null) {
                entity.setTransactionType(TransactionType.valueOf(type));
            }

            String pm = DataTableHelper.getStringOrNull(entry, "paymentMethod");
            if (pm != null) {
                entity.setPaymentMethod(PaymentMethod.valueOf(pm));
            }

            BigDecimal amount = DataTableHelper.toBigDecimal(entry, "amount");
            entity.setAmount(amount);

            String customerId = DataTableHelper.getStringOrNull(entry, "customerId");
            if (customerId == null) {
                customerId = Aliases.getValueOrDefault(entry.get("customerId"));
            }
            entity.setCustomerId(customerId == null ? null : UUID.fromString(customerId));

            entity.setOperatorId(DataTableHelper.getStringOrNull(entry, "operatorId"));

            String sourceType = DataTableHelper.getStringOrNull(entry, "sourceType");
            if (sourceType != null) {
                entity.setSourceType(TransactionSourceType.valueOf(sourceType));
            }

            entity.setSourceId(DataTableHelper.getStringOrNull(entry, "sourceId"));

            Instant recordedAt = DataTableHelper.toInstant(entry, "recordedAt");
            entity.setRecordedAt(recordedAt);

            String idempotency = DataTableHelper.getStringOrNull(entry, "idempotencyKey");
            if (idempotency == null) {
                idempotency = Aliases.getValueOrDefault(entry.get("idempotencyKey"));
            }
            entity.setIdempotencyKey(idempotency == null ? null : UUID.fromString(idempotency));

            // records relationship is handled separately via TransactionRecord transformer and InsertableTransactionRecordRepository

            return entity;
        }
    }

    public static class TransactionRecordJpaEntityTransformer {

        @DataTableType
        public TransactionRecordJpaEntity transform(Map<String, String> entry) {
            TransactionRecordJpaEntity entity = new TransactionRecordJpaEntity();

            String idStr = DataTableHelper.getStringOrNull(entry, "id");
            entity.setId(idStr == null ? null : UUID.fromString(idStr));

            // transaction relationship will be set by the parent when inserting

            String subLedger = DataTableHelper.getStringOrNull(entry, "subLedgerRef");
            if (subLedger == null) {
                subLedger = Aliases.getValueOrDefault(entry.get("subLedgerRef"));
            }
            entity.setSubLedgerRef(subLedger == null ? null : new com.github.jenkaby.bikerental.finance.domain.model.SubLedgerRef(UUID.fromString(subLedger)));

            String ledgerType = DataTableHelper.getStringOrNull(entry, "ledgerType");
            if (ledgerType != null) {
                entity.setLedgerType(LedgerType.valueOf(ledgerType));
            }

            String direction = DataTableHelper.getStringOrNull(entry, "direction");
            if (direction != null) {
                entity.setDirection(EntryDirection.valueOf(direction));
            }

            BigDecimal amount = DataTableHelper.toBigDecimal(entry, "amount");
            entity.setAmount(amount);

            return entity;
        }
    }
}
