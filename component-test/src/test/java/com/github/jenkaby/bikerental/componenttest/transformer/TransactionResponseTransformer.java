package com.github.jenkaby.bikerental.componenttest.transformer;

import com.github.jenkaby.bikerental.componenttest.transformer.shared.Aliases;
import com.github.jenkaby.bikerental.finance.web.query.dto.CustomerTransactionResponse;
import com.github.jenkaby.bikerental.finance.web.query.dto.TransactionBalancesResponse;
import com.github.jenkaby.bikerental.finance.web.query.dto.TransactionDeltasResponse;
import io.cucumber.java.DataTableType;

import java.util.Map;
import java.util.Optional;

public class TransactionResponseTransformer {

    @DataTableType
    public CustomerTransactionResponse transform(Map<String, String> entry) {
        var sourceIdRaw = DataTableHelper.getStringOrNull(entry, "sourceId");
        var sourceId = Optional.ofNullable(sourceIdRaw)
                .map(Aliases::getValueOrDefault)
                .orElse(null);

        return new CustomerTransactionResponse(
                Aliases.getCustomerId(entry.get("customerId")),
                DataTableHelper.toBigDecimal(entry, "amount"),
                DataTableHelper.getStringOrNull(entry, "type"),
                DataTableHelper.getStringOrNull(entry, "direction"),
                DataTableHelper.parseLocalDateTimeToInstant(entry, "recordedAt"),
                DataTableHelper.getStringOrNull(entry, "paymentMethod"),
                DataTableHelper.getStringOrNull(entry, "reason"),
                DataTableHelper.getStringOrNull(entry, "sourceType"),
                sourceId,
                new TransactionDeltasResponse(
                        DataTableHelper.toBigDecimal(entry, "walletDelta"),
                        DataTableHelper.toBigDecimal(entry, "holdDelta"),
                        DataTableHelper.toBigDecimal(entry, "externalDelta")),
                new TransactionBalancesResponse(
                        DataTableHelper.toBigDecimal(entry, "walletBalanceAfter"),
                        DataTableHelper.toBigDecimal(entry, "holdBalanceAfter"))
        );
    }
}
