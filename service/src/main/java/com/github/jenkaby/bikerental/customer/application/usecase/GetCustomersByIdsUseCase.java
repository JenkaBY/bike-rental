package com.github.jenkaby.bikerental.customer.application.usecase;

import com.github.jenkaby.bikerental.customer.domain.model.Customer;

import java.util.List;
import java.util.UUID;

public interface GetCustomersByIdsUseCase {

    List<Customer> execute(List<UUID> ids);
}