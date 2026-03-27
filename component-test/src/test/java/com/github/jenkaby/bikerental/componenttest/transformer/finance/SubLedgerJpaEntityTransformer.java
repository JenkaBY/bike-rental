package com.github.jenkaby.bikerental.componenttest.transformer.finance;

import com.github.jenkaby.bikerental.componenttest.transformer.DataTableHelper;
import com.github.jenkaby.bikerental.componenttest.transformer.shared.Aliases;
import com.github.jenkaby.bikerental.finance.domain.model.LedgerType;
import com.github.jenkaby.bikerental.finance.infrastructure.persistence.entity.AccountJpaEntity;
import com.github.jenkaby.bikerental.finance.infrastructure.persistence.entity.SubLedgerJpaEntity;
import io.cucumber.java.DataTableType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class SubLedgerJpaEntityTransformer {

    @DataTableType
    public SubLedgerJpaEntity transform(Map<String, String> entry) {
        SubLedgerJpaEntity entity = new SubLedgerJpaEntity();

        // id: accept alias or raw UUID
        UUID id = Optional.ofNullable(entry.get("id"))
                .map(Aliases::getValue)
                .map(UUID::fromString)
                .orElse(null);
        entity.setId(id);

        // account reference: accept alias or raw UUID and set minimal AccountJpaEntity with id only
        String accountIdStr = DataTableHelper.getStringOrNull(entry, "accountId");
        if (accountIdStr != null && !accountIdStr.isBlank()) {
            String resolved = Optional.ofNullable(Aliases.getValue(accountIdStr)).orElse(accountIdStr);
            UUID accountId = UUID.fromString(resolved);
            AccountJpaEntity account = new AccountJpaEntity();
            account.setId(accountId);
            entity.setAccount(account);
        }

        var ledgerType = LedgerType.valueOf(DataTableHelper.getStringOrNull(entry, "ledgerType"));
        entity.setLedgerType(ledgerType);

        // balance (non-nullable in JPA entity) — default to zero when absent
        BigDecimal balance = DataTableHelper.toBigDecimal(entry, "balance");
        entity.setBalance(Optional.ofNullable(balance).orElse(BigDecimal.ZERO));

        // audit timestamps
        Instant createdAt = Optional.ofNullable(DataTableHelper.toInstant(entry, "createdAt")).orElse(Instant.now());
        entity.setCreatedAt(createdAt);
        Instant updatedAt = Optional.ofNullable(DataTableHelper.toInstant(entry, "updatedAt")).orElse(createdAt);
        entity.setUpdatedAt(updatedAt);

        return entity;
    }
}

