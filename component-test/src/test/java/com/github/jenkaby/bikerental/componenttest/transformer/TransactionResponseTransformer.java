package com.github.jenkaby.bikerental.componenttest.transformer;

import com.github.jenkaby.bikerental.componenttest.transformer.shared.Aliases;
import com.github.jenkaby.bikerental.finance.web.query.dto.TransactionResponse;
import io.cucumber.java.DataTableType;

import java.util.Map;
import java.util.Optional;

public class TransactionResponseTransformer {

    @DataTableType
    public TransactionResponse transform(Map<String, String> entry) {
        var sourceIdRaw = DataTableHelper.getStringOrNull(entry, "sourceId");
        var sourceId = Optional.ofNullable(sourceIdRaw)
                .map(Aliases::getValueOrDefault)
                .orElse(null);

        return new TransactionResponse(
                Aliases.getCustomerId(entry.get("customerId")),
                DataTableHelper.toBigDecimal(entry, "amount"),
                DataTableHelper.getStringOrNull(entry, "type"),
                DataTableHelper.parseLocalDateTimeToInstant(entry, "recordedAt"),
                DataTableHelper.getStringOrNull(entry, "paymentMethod"),
                DataTableHelper.getStringOrNull(entry, "reason"),
                DataTableHelper.getStringOrNull(entry, "sourceType"),
                sourceId
        );
    }
}
