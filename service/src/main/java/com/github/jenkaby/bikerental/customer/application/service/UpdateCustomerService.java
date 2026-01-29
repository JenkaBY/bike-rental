package com.github.jenkaby.bikerental.customer.application.service;

import com.github.jenkaby.bikerental.customer.application.mapper.CustomerCommandToDomainMapper;
import com.github.jenkaby.bikerental.customer.application.usecase.UpdateCustomerUseCase;
import com.github.jenkaby.bikerental.customer.domain.exception.DuplicatePhoneException;
import com.github.jenkaby.bikerental.customer.domain.model.Customer;
import com.github.jenkaby.bikerental.customer.domain.repository.CustomerRepository;
import com.github.jenkaby.bikerental.customer.shared.mapper.PhoneNumberMapper;
import com.github.jenkaby.bikerental.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class UpdateCustomerService implements UpdateCustomerUseCase {

    private final CustomerRepository repository;
    private final CustomerCommandToDomainMapper mapper;
    private final PhoneNumberMapper phoneMapper;

    UpdateCustomerService(
            CustomerRepository repository,
            CustomerCommandToDomainMapper mapper,
            PhoneNumberMapper phoneMapper) {
        this.repository = repository;
        this.mapper = mapper;
        this.phoneMapper = phoneMapper;
    }

    @Override
    @Transactional
    public Customer execute(UpdateCustomerCommand command) {
        var existingCustomer = repository.findById(command.customerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer", command.customerId().toString()));

        var phoneNumber = phoneMapper.toPhoneNumber(command.phone());

        if (!existingCustomer.getPhone().value().equals(phoneNumber.value())) {
            var customerWithPhone = repository.findByPhone(phoneNumber.value());
            if (customerWithPhone.isPresent() && !customerWithPhone.get().getId().equals(command.customerId())) {
                throw new DuplicatePhoneException(Customer.class.getSimpleName(), phoneNumber.value());
            }
        }

        Customer updatedCustomer = mapper.toCustomer(command);

        return repository.save(updatedCustomer);
    }
}
