package com.github.jenkaby.bikerental.componenttest.steps.finance;

import com.github.jenkaby.bikerental.componenttest.context.ScenarioContext;
import com.github.jenkaby.bikerental.customer.web.query.dto.CustomerResponse;
import com.github.jenkaby.bikerental.finance.web.query.dto.CustomerAccountBalancesResponse;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.SoftAssertions;

@Slf4j
@RequiredArgsConstructor
public class BalancesWebSteps {

    private final ScenarioContext scenarioContext;

    @Then("the balances response contains")
    public void theBalancesResponseContains(CustomerAccountBalancesResponse expected) {
        var actual = scenarioContext.getResponseBody(CustomerAccountBalancesResponse.class);
        log.info("Balances response: {}", actual);

        var softly = new SoftAssertions();
        softly.assertThat(actual.walletBalance())
                .as("walletBalance")
                .isEqualByComparingTo(expected.walletBalance());
        softly.assertThat(actual.holdBalance())
                .as("holdBalance")
                .isEqualByComparingTo(expected.holdBalance());
        softly.assertThat(actual.lastUpdatedAt())
                .as("lastUpdatedAt")
                .isNotNull();
        softly.assertAll();
    }

    @When("customerId is extracted from the response and stored as 'requestedObjectId'")
    public void customerIdIsExtractedFromTheResponseAndStoredAsRequestedObjectId() {
        CustomerResponse actual = scenarioContext.getResponseBody(CustomerResponse.class);
        scenarioContext.setRequestedObjectId(actual.id().toString());

    }
}
