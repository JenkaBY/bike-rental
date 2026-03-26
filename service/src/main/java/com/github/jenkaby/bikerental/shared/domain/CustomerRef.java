package com.github.jenkaby.bikerental.shared.domain;

import java.util.UUID;

public record CustomerRef(UUID id) {

    public CustomerRef {
        if (id == null) {
            throw new IllegalArgumentException("Customer id must not be null");
        }
    }

    public static CustomerRef of(UUID id) {
        return id == null ? null : new CustomerRef(id);
    }
}
