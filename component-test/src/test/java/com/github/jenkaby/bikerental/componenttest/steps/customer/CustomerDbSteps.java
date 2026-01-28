package com.github.jenkaby.bikerental.componenttest.steps.customer;

import com.github.jenkaby.bikerental.customer.infrastructure.persistence.entity.CustomerJpaEntity;
import com.github.jenkaby.bikerental.customer.infrastructure.persistence.repository.CustomerJpaRepository;
import io.cucumber.java.en.Given;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@RequiredArgsConstructor
public class CustomerDbSteps {

    private final CustomerJpaRepository customerJpaRepository;

    @Given("a customer exists in the database with the following data")
    public void aCustomerExistsInTheDatabaseWithTheFollowingData(CustomerJpaEntity customer) {
        log.info("Creating customer in database: {}", customer);

        var saved = customerJpaRepository.save(customer);

        log.debug("Customer created in database with ID: {}", saved);
    }
}
