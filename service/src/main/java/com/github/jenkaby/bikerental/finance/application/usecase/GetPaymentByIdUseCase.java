package com.github.jenkaby.bikerental.finance.application.usecase;

import com.github.jenkaby.bikerental.finance.domain.model.Payment;

import java.util.UUID;

public interface GetPaymentByIdUseCase {
    Payment execute(UUID id);
}
