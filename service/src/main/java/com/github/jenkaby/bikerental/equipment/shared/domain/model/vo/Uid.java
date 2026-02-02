package com.github.jenkaby.bikerental.equipment.shared.domain.model.vo;

public record Uid(String value) {
    public Uid {
        if (value != null && value.isBlank()) {
            throw new IllegalArgumentException("UID cannot be blank");
        }
    }
}
