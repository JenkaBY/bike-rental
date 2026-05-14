package com.github.jenkaby.bikerental.customer.application.service;

import com.github.jenkaby.bikerental.customer.application.usecase.GetCustomersByIdsUseCase;
import com.github.jenkaby.bikerental.customer.domain.model.Customer;
import com.github.jenkaby.bikerental.customer.domain.repository.CustomerRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
class GetCustomersByIdsService implements GetCustomersByIdsUseCase {

    private final CustomerRepository repository;

    GetCustomersByIdsService(CustomerRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Customer> execute(List<UUID> ids) {
        return repository.findByIds(ids);
    }
}