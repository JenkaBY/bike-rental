package com.github.jenkaby.bikerental.componenttest.transformer.finance;

import com.github.jenkaby.bikerental.componenttest.transformer.DataTableHelper;
import com.github.jenkaby.bikerental.componenttest.transformer.shared.Aliases;
import com.github.jenkaby.bikerental.finance.domain.model.EntryDirection;
import com.github.jenkaby.bikerental.finance.domain.model.LedgerType;
import com.github.jenkaby.bikerental.finance.domain.model.SubLedgerRef;
import com.github.jenkaby.bikerental.finance.infrastructure.persistence.entity.TransactionRecordJpaEntity;
import io.cucumber.java.DataTableType;

import java.util.Map;
import java.util.UUID;

public class TransactionRecordJpaEntityTransformer {

    @DataTableType
    public TransactionRecordJpaEntity transform(Map<String, String> entry) {
        TransactionRecordJpaEntity entity = new TransactionRecordJpaEntity();

        var idString = DataTableHelper.getStringOrNull(entry, "id");
        entity.setId(idString == null ? null : UUID.fromString(idString));

        entity.setSubLedgerRef(new SubLedgerRef(Aliases.getUuid(entry.get("subLedger"))));
        entity.setLedgerType(LedgerType.valueOf(DataTableHelper.getStringOrNull(entry, "ledgerType")));
        entity.setDirection(EntryDirection.valueOf(DataTableHelper.getStringOrNull(entry, "direction")));
        entity.setAmount(DataTableHelper.toBigDecimal(entry, "amount"));

        return entity;
    }
}

