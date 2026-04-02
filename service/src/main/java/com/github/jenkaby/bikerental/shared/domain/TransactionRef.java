package com.github.jenkaby.bikerental.shared.domain;

import java.util.UUID;

public record TransactionRef(UUID id) {

    public TransactionRef {
        if (id == null) {
            throw new IllegalArgumentException("Transaction id must not be null");
        }
    }

    public static TransactionRef of(UUID id) {
        return new TransactionRef(id);
    }
}
