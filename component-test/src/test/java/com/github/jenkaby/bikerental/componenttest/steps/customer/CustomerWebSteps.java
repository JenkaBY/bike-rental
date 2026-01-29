package com.github.jenkaby.bikerental.componenttest.steps.customer;

import com.github.jenkaby.bikerental.componenttest.context.ScenarioContext;
import com.github.jenkaby.bikerental.customer.web.command.dto.CustomerRequest;
import com.github.jenkaby.bikerental.customer.web.query.dto.CustomerResponse;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.SoftAssertions;


@Slf4j
@RequiredArgsConstructor
public class CustomerWebSteps {

    private final ScenarioContext scenarioContext;


    @Given("a customer request with the following data")
    public void aCustomerRequestWithTheFollowingData(CustomerRequest customerRequest) {
        log.info("Preparing customer request with data: {}", customerRequest);

        scenarioContext.setRequestBody(customerRequest);
    }

    @Given("a customer update request with the following data")
    public void aCustomerUpdateRequestWithTheFollowingData(CustomerRequest updateRequest) {
        log.info("Preparing customer update request with data: {}", updateRequest);

        scenarioContext.setRequestBody(updateRequest);
    }

    @SneakyThrows
    @Then("the response matches expected customer")
    public void theResponseMatchesExpectedCustomer(CustomerResponse expectedCustomer) {
        var actualCustomer = scenarioContext.getResponseBody(CustomerResponse.class);

        var softly = new SoftAssertions();
        softly.assertThat(actualCustomer.id())
                .as("Customer ID")
                .isEqualTo(expectedCustomer.id());
        softly.assertThat(actualCustomer.phone())
                .as("Customer phone")
                .isEqualTo(expectedCustomer.phone());
        softly.assertThat(actualCustomer.firstName())
                .as("Customer firstName")
                .isEqualTo(expectedCustomer.firstName());
        softly.assertThat(actualCustomer.lastName())
                .as("Customer lastName")
                .isEqualTo(expectedCustomer.lastName());
        softly.assertThat(actualCustomer.email())
                .as("Customer email")
                .isEqualTo(expectedCustomer.email());
        softly.assertThat(actualCustomer.birthDate())
                .as("Customer birthDate")
                .isEqualTo(expectedCustomer.birthDate());
        softly.assertThat(actualCustomer.comments())
                .as("Customer comments")
                .isEqualTo(expectedCustomer.comments());
        softly.assertAll();
    }
}
