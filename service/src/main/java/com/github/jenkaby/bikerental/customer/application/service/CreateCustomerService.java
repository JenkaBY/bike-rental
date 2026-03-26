package com.github.jenkaby.bikerental.customer.application.service;

import com.github.jenkaby.bikerental.customer.application.mapper.CustomerCommandToDomainMapper;
import com.github.jenkaby.bikerental.customer.application.usecase.CreateCustomerUseCase;
import com.github.jenkaby.bikerental.customer.domain.exception.DuplicatePhoneException;
import com.github.jenkaby.bikerental.customer.domain.model.Customer;
import com.github.jenkaby.bikerental.customer.domain.repository.CustomerRepository;
import com.github.jenkaby.bikerental.customer.shared.mapper.PhoneNumberMapper;
import com.github.jenkaby.bikerental.shared.domain.CustomerRef;
import com.github.jenkaby.bikerental.shared.domain.event.CustomerRegistered;
import com.github.jenkaby.bikerental.shared.infrastructure.messaging.EventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class CreateCustomerService implements CreateCustomerUseCase {

    static final String CUSTOMER_EVENTS_DESTINATION = "customer-events";

    private final CustomerRepository repository;
    private final CustomerCommandToDomainMapper mapper;
    private final PhoneNumberMapper phoneMapper;
    private final EventPublisher eventPublisher;

    CreateCustomerService(
            CustomerRepository repository,
            CustomerCommandToDomainMapper mapper,
            PhoneNumberMapper phoneMapper,
            EventPublisher eventPublisher) {
        this.repository = repository;
        this.mapper = mapper;
        this.phoneMapper = phoneMapper;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public Customer execute(CreateCustomerCommand command) {
        var phoneNumber = phoneMapper.toPhoneNumber(command.phone());

        if (repository.existsByPhone(phoneNumber.value())) {
            throw new DuplicatePhoneException(Customer.class.getSimpleName(), phoneNumber.value());
        }

        Customer customer = mapper.toCustomer(command);
        Customer saved = repository.save(customer);

        eventPublisher.publish(CUSTOMER_EVENTS_DESTINATION, new CustomerRegistered(CustomerRef.of(saved.getId())));

        return saved;
    }
}
