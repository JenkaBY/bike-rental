package com.github.jenkaby.bikerental.finance;

import com.github.jenkaby.bikerental.shared.domain.TransactionRef;

import java.time.Instant;

public record HoldInfo(TransactionRef transactionRef, Instant recordedAt) {
}
