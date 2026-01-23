package com.github.jenkaby.bikerental.customer.application.service;

import com.github.jenkaby.bikerental.customer.application.usecase.CreateCustomerUseCase;
import com.github.jenkaby.bikerental.customer.domain.exception.DuplicatePhoneException;
import com.github.jenkaby.bikerental.customer.domain.model.Customer;
import com.github.jenkaby.bikerental.customer.domain.model.vo.EmailAddress;
import com.github.jenkaby.bikerental.customer.domain.model.vo.PhoneNumber;
import com.github.jenkaby.bikerental.customer.domain.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class CreateCustomerService implements CreateCustomerUseCase {

    private final CustomerRepository repository;

    CreateCustomerService(CustomerRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public Customer execute(CreateCustomerCommand command) {
        var phoneNumber = new PhoneNumber(command.phone());

        if (repository.existsByPhone(phoneNumber.value())) {
            throw new DuplicatePhoneException(Customer.class.getSimpleName(), phoneNumber.value());
        }

        Customer customer = Customer.builder()
                .phone(phoneNumber)
                .firstName(command.firstName())
                .lastName(command.lastName())
                .email(new EmailAddress(command.email()))
                .birthDate(command.birthDate())
                .build();

        return repository.save(customer);
    }
}
