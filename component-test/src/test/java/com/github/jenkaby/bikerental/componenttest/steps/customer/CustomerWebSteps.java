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

import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


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

        assertCustomer(actualCustomer, expectedCustomer);
    }

    @Then("the batch customer response contains")
    public void theBatchCustomerResponseContains(List<CustomerResponse> expectedResponses) {
        var actual = scenarioContext.getResponseAsList(CustomerResponse.class).stream()
                .sorted(Comparator.comparing(CustomerResponse::phone))
                .toList();
        var expectedSorted = expectedResponses.stream()
                .sorted(Comparator.comparing(CustomerResponse::phone))
                .toList();

        assertThat(actual).as("batch customer response size").hasSize(expectedSorted.size());

        assertThat(actual).zipSatisfy(expectedSorted, this::assertCustomer);
    }

    private void assertCustomer(CustomerResponse actual, CustomerResponse expected) {
        var softly = new SoftAssertions();
        if (expected.id() == null) {
            softly.assertThat(actual.id()).as("Customer ID").isNotNull();
        } else {
            softly.assertThat(actual.id()).as("Customer ID").isEqualTo(expected.id());
        }

        softly.assertThat(actual.phone()).as("Customer phone").isEqualTo(expected.phone());
        softly.assertThat(actual.firstName()).as("Customer firstName").isEqualTo(expected.firstName());
        softly.assertThat(actual.lastName()).as("Customer lastName").isEqualTo(expected.lastName());
        softly.assertThat(actual.email()).as("Customer email").isEqualTo(expected.email());
        softly.assertThat(actual.birthDate()).as("Customer birthDate").isEqualTo(expected.birthDate());
        softly.assertThat(actual.comments()).as("Customer comments").isEqualTo(expected.comments());

        softly.assertAll();
    }

    @Then("the batch customer response is empty")
    public void theBatchCustomerResponseIsEmpty() {
        assertThat(scenarioContext.getResponseAsList(CustomerResponse.class)).isEmpty();
    }
}
