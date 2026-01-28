package com.github.jenkaby.bikerental.customer.application.usecase;

import com.github.jenkaby.bikerental.customer.domain.model.Customer;

import java.time.LocalDate;


public interface CreateCustomerUseCase {

    Customer execute(CreateCustomerCommand command);

    record CreateCustomerCommand(
            String phone,
            String firstName,
            String lastName,
            String email,
            LocalDate birthDate
    ) {
    }
}
