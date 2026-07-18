package com.github.jenkaby.bikerental.finance.domain.model;

import java.util.Arrays;
import java.util.Optional;

public enum TransactionSortField {
    RECORDED_AT("recordedAt"),
    AMOUNT("amount"),
    TYPE("type");

    private final String apiName;

    TransactionSortField(String apiName) {
        this.apiName = apiName;
    }

    public String apiName() {
        return apiName;
    }

    public static Optional<TransactionSortField> byApiName(String apiName) {
        return Arrays.stream(values())
                .filter(field -> field.apiName.equals(apiName))
                .findFirst();
    }
}
