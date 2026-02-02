package com.github.jenkaby.bikerental.equipment.shared.domain.model.vo;

public record SerialNumber(String value) {
    public SerialNumber {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Serial number cannot be null or blank");
        }
    }
}
