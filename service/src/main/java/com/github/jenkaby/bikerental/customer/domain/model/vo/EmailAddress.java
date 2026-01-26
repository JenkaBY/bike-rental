package com.github.jenkaby.bikerental.customer.domain.model.vo;

import java.util.regex.Pattern;

public record EmailAddress(String value) {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9_+&*-]+(?:\\.[A-Za-z0-9_+&*-]+)*@(?:[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?\\.)+[A-Za-z]{2,}$"
    );

    public EmailAddress {
        if (value != null && !value.isBlank()) {
            if (value.length() > 254) {
                throw new IllegalArgumentException("Email address is too long (max 254 characters)");
            }

            if (!EMAIL_PATTERN.matcher(value).matches()) {
                throw new IllegalArgumentException("Invalid email format: " + value);
            }
        }
    }
}
