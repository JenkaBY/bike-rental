package com.github.jenkaby.bikerental.finance.application.usecase;

import com.github.jenkaby.bikerental.finance.domain.model.Payment;

import java.util.List;

public interface GetPaymentsByRentalIdUseCase {
    List<Payment> execute(Long rentalId);
}
