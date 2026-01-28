package com.github.jenkaby.bikerental.customer.application.service;

import com.github.jenkaby.bikerental.customer.CustomerInfo;
import com.github.jenkaby.bikerental.customer.application.config.CustomerSearchProperties;
import com.github.jenkaby.bikerental.customer.application.mapper.CustomerMapper;
import com.github.jenkaby.bikerental.customer.application.usecase.CustomerQueryUseCase;
import com.github.jenkaby.bikerental.customer.domain.repository.CustomerRepository;
import com.github.jenkaby.bikerental.customer.domain.util.PhoneUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
class CustomerQueryService implements CustomerQueryUseCase {

    private final CustomerRepository repository;
    private final CustomerMapper mapper;
    private final CustomerSearchProperties properties;

    @Override
    public Optional<CustomerInfo> findById(UUID id) {
        return repository.findById(id).map(mapper::toInfo);
    }

    @Override
    public Optional<CustomerInfo> findByPhone(String phone) {
        String normalized = PhoneUtil.normalize(phone);
        return repository.findByPhone(normalized).map(mapper::toInfo);
    }

    @Override
    public List<CustomerInfo> searchByPhone(String phone) {
        var normalized = PhoneUtil.normalize(phone);
        return repository.searchByPhone(normalized, properties.searchLimitResult()).stream()
                .map(mapper::toInfo)
                .toList();
    }
}
