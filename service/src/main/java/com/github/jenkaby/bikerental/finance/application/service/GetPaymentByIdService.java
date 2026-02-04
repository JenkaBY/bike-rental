package com.github.jenkaby.bikerental.finance.application.service;

import com.github.jenkaby.bikerental.finance.application.usecase.GetPaymentByIdUseCase;
import com.github.jenkaby.bikerental.finance.domain.model.Payment;
import com.github.jenkaby.bikerental.finance.domain.repository.PaymentRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class GetPaymentByIdService implements GetPaymentByIdUseCase {

    private final PaymentRepository repository;

    public GetPaymentByIdService(PaymentRepository repository) {
        this.repository = repository;
    }

    @Override
    public Payment execute(UUID id) {
        return repository.get(id);
    }
}
