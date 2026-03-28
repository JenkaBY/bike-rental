package com.github.jenkaby.bikerental.componenttest.steps.finance;

import com.github.jenkaby.bikerental.componenttest.context.ScenarioContext;
import com.github.jenkaby.bikerental.finance.web.command.dto.RecordDepositRequest;
import com.github.jenkaby.bikerental.finance.web.command.dto.RecordDepositResponse;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

@Slf4j
@RequiredArgsConstructor
public class DepositWebSteps {

    private final ScenarioContext scenarioContext;


    @Given("the deposit request is prepared with the following data")
    public void theDepositRequestIsPreparedWithTheFollowingData(RecordDepositRequest request) {
        log.info("Preparing deposit request: {}", request);
        scenarioContext.setRequestBody(request);
    }

    @Then("the deposit response contains a transactionId")
    public void theDepositResponseContainsATransactionId() {
        RecordDepositResponse body = scenarioContext.getResponseBody(RecordDepositResponse.class);

        assertThat(body.transactionId())
                .as("transactionId must be present in deposit response").isNotNull();
        assertThat(body.recordedAt()).isCloseTo(Instant.now(), within(Duration.ofSeconds(2)));
        scenarioContext.setRequestedObjectId(body.transactionId().toString());
    }
}
