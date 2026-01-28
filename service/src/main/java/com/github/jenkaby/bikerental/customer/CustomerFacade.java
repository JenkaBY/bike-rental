package com.github.jenkaby.bikerental.customer;

import java.util.Optional;
import java.util.UUID;

public interface CustomerFacade {

    Optional<CustomerInfo> findById(UUID customerId);

    Optional<CustomerInfo> findByPhone(String phone);
}
