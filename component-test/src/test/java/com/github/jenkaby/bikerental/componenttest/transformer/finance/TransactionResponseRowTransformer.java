package com.github.jenkaby.bikerental.componenttest.transformer.finance;

import com.github.jenkaby.bikerental.componenttest.transformer.DataTableHelper;
import com.github.jenkaby.bikerental.componenttest.transformer.shared.Aliases;
import io.cucumber.java.DataTableType;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

public class TransactionResponseRowTransformer {

    @DataTableType
    public ExpectedTransactionRow transform(Map<String, String> row) {
        var customerAlias = DataTableHelper.getStringOrNull(row, "customerId");
        var sourceId = DataTableHelper.getStringOrNull(row, "sourceId");
        return new ExpectedTransactionRow(
                customerAlias == null ? null : Aliases.getValueOrDefault(customerAlias),
                DataTableHelper.getStringOrNull(row, "type"),
                DataTableHelper.toBigDecimal(row, "amount"),
                DataTableHelper.getStringOrNull(row, "sourceType"),
                sourceId == null ? null : Aliases.getValueOrDefault(sourceId),
                DataTableHelper.getSetOrDefault(row, "ledgerTypes", Set.of()));
    }

    public record ExpectedTransactionRow(
            String customerId,
            String type,
            BigDecimal amount,
            String sourceType,
            String sourceId,
            Set<String> ledgerTypes) {
    }
}
