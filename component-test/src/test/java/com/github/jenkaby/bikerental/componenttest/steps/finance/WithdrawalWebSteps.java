package com.github.jenkaby.bikerental.componenttest.steps.finance;

import com.github.jenkaby.bikerental.componenttest.context.ScenarioContext;
import com.github.jenkaby.bikerental.finance.web.command.dto.RecordWithdrawalRequest;
import com.github.jenkaby.bikerental.finance.web.command.dto.TransactionResponse;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Clock;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

@Slf4j
@RequiredArgsConstructor
public class WithdrawalWebSteps {

    private final ScenarioContext scenarioContext;
    private final Clock clock;

    @Given("the withdrawal request is prepared with the following data")
    public void theWithdrawalRequestIsPreparedWithTheFollowingData(RecordWithdrawalRequest request) {
        log.info("Preparing withdrawal request: {}", request);
        scenarioContext.setRequestBody(request);
    }

    @Then("the withdrawal response contains a transactionId")
    public void theWithdrawalResponseContainsATransactionId() {
        TransactionResponse body = scenarioContext.getResponseBody(TransactionResponse.class);

        assertThat(body.transactionId())
                .as("transactionId must be present in withdrawal response").isNotNull();
        assertThat(body.recordedAt()).isCloseTo(clock.instant(), within(Duration.ofSeconds(2)));
        scenarioContext.setRequestedObjectId(body.transactionId().toString());
    }
}
