package com.github.jenkaby.bikerental.componenttest.transformer.finance;

import com.github.jenkaby.bikerental.componenttest.transformer.DataTableHelper;
import com.github.jenkaby.bikerental.componenttest.transformer.shared.Aliases;
import com.github.jenkaby.bikerental.finance.domain.model.EntryDirection;
import com.github.jenkaby.bikerental.finance.domain.model.LedgerType;
import com.github.jenkaby.bikerental.finance.infrastructure.persistence.entity.TransactionJpaEntity;
import com.github.jenkaby.bikerental.finance.infrastructure.persistence.entity.TransactionRecordJpaEntity;
import io.cucumber.java.DataTableType;

import java.util.Map;

public class TransactionRecordJpaEntityTransformer {

    @DataTableType
    public TransactionRecordJpaEntity transform(Map<String, String> entry) {
        TransactionRecordJpaEntity entity = new TransactionRecordJpaEntity();

        var idString = DataTableHelper.getStringOrNull(entry, "id");
        entity.setId(Aliases.getUuid(idString));
        TransactionJpaEntity transaction = new TransactionJpaEntity();
        transaction.setId(Aliases.getUuid(entry.get("transaction")));
        entity.setTransaction(transaction);
        entity.setSubLedgerId(Aliases.getUuid(entry.get("subLedger")));
        entity.setLedgerType(LedgerType.valueOf(DataTableHelper.getStringOrNull(entry, "ledgerType")));
        entity.setDirection(EntryDirection.valueOf(DataTableHelper.getStringOrNull(entry, "direction")));
        entity.setAmount(DataTableHelper.toBigDecimal(entry, "amount"));

        return entity;
    }
}

