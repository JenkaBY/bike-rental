package com.github.jenkaby.bikerental.customer;

import com.github.jenkaby.bikerental.customer.application.usecase.CustomerQueryUseCase;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
class CustomerFacadeImpl implements CustomerFacade {

    private final CustomerQueryUseCase customerQueryUseCase;

    CustomerFacadeImpl(CustomerQueryUseCase customerQueryUseCase) {
        this.customerQueryUseCase = customerQueryUseCase;
    }

    @Override
    public Optional<CustomerInfo> findById(UUID customerId) {
        return customerQueryUseCase.findById(customerId);
    }

    @Override
    public Optional<CustomerInfo> findByPhone(String phone) {
        return customerQueryUseCase.findByPhone(phone);
    }
}
