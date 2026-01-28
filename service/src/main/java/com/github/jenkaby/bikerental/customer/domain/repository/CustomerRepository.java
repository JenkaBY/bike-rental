package com.github.jenkaby.bikerental.customer.domain.repository;

import com.github.jenkaby.bikerental.customer.domain.model.Customer;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository {
    Customer save(Customer customer);

    Optional<Customer> findById(UUID id);

    Optional<Customer> findByPhone(String phone);

    boolean existsByPhone(String phone);

    List<Customer> searchByPhone(String phone, int limit);
}
