package com.github.jenkaby.bikerental.componenttest.steps.customer;

import com.github.jenkaby.bikerental.componenttest.context.ScenarioContext;
import com.github.jenkaby.bikerental.customer.web.command.dto.CreateCustomerRequest;
import io.cucumber.java.en.Given;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@RequiredArgsConstructor
public class CustomerWebSteps {

    private final ScenarioContext scenarioContext;


    @Given("a customer request with the following data")
    public void aCustomerRequestWithTheFollowingData(CreateCustomerRequest customerRequest) {
        log.info("Preparing customer request with data: {}", customerRequest);

        scenarioContext.setRequestBody(customerRequest);
    }
}
