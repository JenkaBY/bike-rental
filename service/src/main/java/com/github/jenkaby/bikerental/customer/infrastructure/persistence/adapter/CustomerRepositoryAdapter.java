package com.github.jenkaby.bikerental.customer.infrastructure.persistence.adapter;

import com.github.jenkaby.bikerental.customer.domain.model.Customer;
import com.github.jenkaby.bikerental.customer.domain.repository.CustomerRepository;
import com.github.jenkaby.bikerental.customer.infrastructure.persistence.mapper.CustomerJpaMapper;
import com.github.jenkaby.bikerental.customer.infrastructure.persistence.repository.CustomerJpaRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
class CustomerRepositoryAdapter implements CustomerRepository {

    private final CustomerJpaRepository repository;
    private final CustomerJpaMapper mapper;

    CustomerRepositoryAdapter(CustomerJpaRepository repository, CustomerJpaMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Customer save(Customer customer) {
        var entity = mapper.toEntity(customer);
        var savedEntity = repository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Customer> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Customer> findByPhone(String phone) {
        return repository.findByPhone(phone).map(mapper::toDomain);
    }

    @Override
    public boolean existsByPhone(String phone) {
        return repository.existsByPhone(phone);
    }

    @Override
    public List<Customer> searchByPhone(String phone, int limit) {
        var pageable = PageRequest.of(0, limit);
        return repository.findByPhoneContaining(phone, pageable).stream()
                .map(mapper::toDomain)
                .toList();
    }
}
