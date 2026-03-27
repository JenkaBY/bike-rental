package com.github.jenkaby.bikerental.componenttest.transformer.finance;

import com.github.jenkaby.bikerental.componenttest.transformer.DataTableHelper;
import com.github.jenkaby.bikerental.componenttest.transformer.shared.Aliases;
import com.github.jenkaby.bikerental.finance.domain.model.AccountType;
import com.github.jenkaby.bikerental.finance.infrastructure.persistence.entity.AccountJpaEntity;
import io.cucumber.java.DataTableType;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class AccountJpaEntityTransformer {

    @DataTableType
    public AccountJpaEntity transform(Map<String, String> entry) {
        AccountJpaEntity entity = new AccountJpaEntity();

        // id: allow alias lookup via Aliases or raw UUID string
        UUID id = Optional.ofNullable(entry.get("id"))
                .map(Aliases::getValue)
                .map(UUID::fromString)
                .orElse(null);
        entity.setId(id);

        // accountType: enum value if provided
        String accountTypeStr = DataTableHelper.getStringOrNull(entry, "accountType");
        AccountType accountType = Optional.ofNullable(accountTypeStr)
                .map(AccountType::valueOf)
                .orElse(null);
        entity.setAccountType(accountType);

        // customerId: use alias helper for known aliases
        String customerIdString = DataTableHelper.getStringOrNull(entry, "customerId");
        UUID customerId = Optional.ofNullable(customerIdString)
                .map(Aliases::getCustomerId)
                .orElse(null);
        entity.setCustomerId(customerId);

        // createdAt: optional, default to now()
        Instant createdAt = Optional.ofNullable(DataTableHelper.toInstant(entry, "createdAt")).orElse(Instant.now());
        entity.setCreatedAt(createdAt);

        // subLedgers: left as default (empty list) — populate with separate transformers if needed

        return entity;
    }
}

