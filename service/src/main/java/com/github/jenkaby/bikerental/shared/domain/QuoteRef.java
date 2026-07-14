package com.github.jenkaby.bikerental.shared.domain;

import java.util.UUID;

public record QuoteRef(UUID id) {

    public QuoteRef {
        if (id == null) {
            throw new IllegalArgumentException("Quote id must not be null");
        }
    }

    public static QuoteRef of(UUID id) {
        return new QuoteRef(id);
    }
}
