package com.github.jenkaby.bikerental.finance.domain.model;

public enum AccountType {
    SYSTEM,
    CUSTOMER;

    public boolean isSystem() {
        return this == SYSTEM;
    }
}
