package com.github.jenkaby.bikerental.shared.domain;

import java.util.UUID;

public record IdempotencyKey(UUID id) {

    public IdempotencyKey {
        if (id == null) {
            throw new IllegalArgumentException("Idempotency key must not be null");
        }
    }

    public static IdempotencyKey of(UUID id) {
        return new IdempotencyKey(id);
    }
}
