package com.github.jenkaby.bikerental.customer.application.service;

import com.github.jenkaby.bikerental.customer.CustomerInfo;
import com.github.jenkaby.bikerental.customer.application.mapper.CustomerMapper;
import com.github.jenkaby.bikerental.customer.application.usecase.CustomerQueryUseCase;
import com.github.jenkaby.bikerental.customer.domain.repository.CustomerRepository;
import com.github.jenkaby.bikerental.customer.domain.util.PhoneUtil;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
class CustomerQueryService implements CustomerQueryUseCase {

    private final CustomerRepository repository;
    private final CustomerMapper mapper;

    CustomerQueryService(CustomerRepository repository, CustomerMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Optional<CustomerInfo> findById(UUID id) {
        return repository.findById(id).map(mapper::toInfo);
    }

    @Override
    public Optional<CustomerInfo> findByPhone(String phone) {
        String normalized = PhoneUtil.normalize(phone);
        return repository.findByPhone(normalized).map(mapper::toInfo);
    }
}
