package com.github.jenkaby.bikerental.customer.application.usecase;

import com.github.jenkaby.bikerental.customer.CustomerInfo;

import java.util.Optional;
import java.util.UUID;

public interface CustomerQueryUseCase {

    Optional<CustomerInfo> findById(UUID id);

    Optional<CustomerInfo> findByPhone(String phone);
}
