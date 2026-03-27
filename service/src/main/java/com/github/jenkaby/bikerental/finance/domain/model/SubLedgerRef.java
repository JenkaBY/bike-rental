package com.github.jenkaby.bikerental.finance.domain.model;

import org.jspecify.annotations.NonNull;

import java.util.UUID;

public record SubLedgerRef(@NonNull UUID id) {

    public SubLedgerRef {
        if (id == null) {
            throw new IllegalArgumentException("SubLedger id must not be null");
        }
    }
}
