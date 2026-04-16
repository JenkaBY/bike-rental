package com.github.jenkaby.bikerental.customer.application.service;

import com.github.jenkaby.bikerental.customer.application.usecase.GetCustomerByIdUseCase;
import com.github.jenkaby.bikerental.customer.domain.model.Customer;
import com.github.jenkaby.bikerental.customer.domain.repository.CustomerRepository;
import com.github.jenkaby.bikerental.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
class GetCustomerByIdService implements GetCustomerByIdUseCase {

    private final CustomerRepository repository;

    @Override
    public Customer getById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(Customer.class, id));
    }
}
