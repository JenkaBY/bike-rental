package com.github.jenkaby.bikerental.customer.domain.model.vo;

import com.github.jenkaby.bikerental.customer.domain.util.PhoneUtil;

public record PhoneNumber(String value) {
    public PhoneNumber {
        value = PhoneUtil.normalize(value);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Phone number cannot be empty");
        }
    }
}
