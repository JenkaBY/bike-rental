package com.github.jenkaby.bikerental.customer.domain.model.vo;

public record EmailAddress(String value) {
    public EmailAddress {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
    }
}
