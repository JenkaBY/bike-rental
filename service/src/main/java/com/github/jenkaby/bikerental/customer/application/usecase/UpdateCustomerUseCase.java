package com.github.jenkaby.bikerental.customer.application.usecase;

import com.github.jenkaby.bikerental.customer.domain.model.Customer;

import java.time.LocalDate;
import java.util.UUID;

public interface UpdateCustomerUseCase {

    Customer execute(UpdateCustomerCommand command);

    record UpdateCustomerCommand(
            UUID customerId,
            String phone,
            String firstName,
            String lastName,
            String email,
            LocalDate birthDate,
            String comments
    ) {
    }
}
