package com.github.jenkaby.bikerental.componenttest.transformer.finance;

import com.github.jenkaby.bikerental.componenttest.transformer.DataTableHelper;
import com.github.jenkaby.bikerental.componenttest.transformer.shared.Aliases;
import io.cucumber.java.DataTableType;

import java.math.BigDecimal;
import java.util.Map;

public class TransactionDetailsResponseRowTransformer {

    @DataTableType
    public ExpectedTransactionDetailsRow transformDetails(Map<String, String> row) {
        var customerAlias = DataTableHelper.getStringOrNull(row, "customerId");
        return new ExpectedTransactionDetailsRow(
                customerAlias == null ? null : Aliases.getValueOrDefault(customerAlias),
                DataTableHelper.getStringOrNull(row, "type"),
                DataTableHelper.toBigDecimal(row, "amount"),
                DataTableHelper.toBigDecimal(row, "walletDelta"),
                DataTableHelper.toBigDecimal(row, "holdDelta"),
                DataTableHelper.toBigDecimal(row, "externalDelta"),
                DataTableHelper.toBigDecimal(row, "walletBalance"),
                DataTableHelper.toBigDecimal(row, "holdBalance"));
    }

    @DataTableType
    public ExpectedTransactionEntryRow transformEntry(Map<String, String> row) {
        return new ExpectedTransactionEntryRow(
                DataTableHelper.getStringOrNull(row, "ledgerType"),
                DataTableHelper.getStringOrNull(row, "direction"),
                DataTableHelper.toBigDecimal(row, "amount"),
                DataTableHelper.toBigDecimal(row, "signedDelta"),
                DataTableHelper.toBigDecimal(row, "balanceAfter"),
                DataTableHelper.toBooleanOrNull(row, "systemLedger"));
    }

    public record ExpectedTransactionDetailsRow(
            String customerId,
            String type,
            BigDecimal amount,
            BigDecimal walletDelta,
            BigDecimal holdDelta,
            BigDecimal externalDelta,
            BigDecimal walletBalance,
            BigDecimal holdBalance) {
    }

    public record ExpectedTransactionEntryRow(
            String ledgerType,
            String direction,
            BigDecimal amount,
            BigDecimal signedDelta,
            BigDecimal balanceAfter,
            Boolean systemLedger) {
    }
}
