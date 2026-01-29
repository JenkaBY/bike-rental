package com.github.jenkaby.bikerental.componenttest.steps.customer;

import com.github.jenkaby.bikerental.componenttest.config.db.repository.InsertableCustomerRepository;
import com.github.jenkaby.bikerental.customer.infrastructure.persistence.entity.CustomerJpaEntity;
import io.cucumber.java.en.Given;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;


@Slf4j
@RequiredArgsConstructor
public class CustomerDbSteps {

    private final InsertableCustomerRepository customerJpaRepository;

    @Given("(a )customer(s) exist(s) in the database with the following data")
    public void aCustomerExistsInTheDatabaseWithTheFollowingData(List<CustomerJpaEntity> customers) {
        log.info("Creating customer in database: {}", customers);

        customerJpaRepository.insertAll(customers);
    }
}
