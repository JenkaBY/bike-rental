package com.github.jenkaby.bikerental.customer.application.usecase;

import com.github.jenkaby.bikerental.customer.domain.model.Customer;

import java.util.UUID;

public interface GetCustomerByIdUseCase {

    Customer getById(UUID id);

}
