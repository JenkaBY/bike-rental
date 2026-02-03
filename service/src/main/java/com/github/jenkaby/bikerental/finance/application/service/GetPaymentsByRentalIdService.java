package com.github.jenkaby.bikerental.finance.application.service;

import com.github.jenkaby.bikerental.finance.application.usecase.GetPaymentsByRentalIdUseCase;
import com.github.jenkaby.bikerental.finance.domain.model.Payment;
import com.github.jenkaby.bikerental.finance.domain.repository.PaymentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GetPaymentsByRentalIdService implements GetPaymentsByRentalIdUseCase {

    private final PaymentRepository repository;

    public GetPaymentsByRentalIdService(PaymentRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Payment> execute(Long rentalId) {
        return repository.getForRental(rentalId);
    }
}
