package com.github.jenkaby.bikerental.componenttest.steps.finance;

import com.github.jenkaby.bikerental.componenttest.context.ScenarioContext;
import com.github.jenkaby.bikerental.componenttest.steps.common.WebRequestSteps;
import com.github.jenkaby.bikerental.finance.web.command.dto.AdjustmentRequest;
import com.github.jenkaby.bikerental.finance.web.command.dto.AdjustmentResponse;
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
public class AdjustmentWebSteps {

    private final ScenarioContext scenarioContext;
    private final WebRequestSteps webRequestSteps;
    private final Clock clock;

    @Given("the adjustment request is prepared with the following data")
    public void theAdjustmentRequestIsPreparedWithTheFollowingData(AdjustmentRequest request) {
        log.info("Preparing adjustment request: {}", request);
        scenarioContext.setRequestBody(request);
    }

    @Then("the adjustment response contains a transactionId")
    public void theAdjustmentResponseContainsATransactionId() {
        AdjustmentResponse body = scenarioContext.getResponseBody(AdjustmentResponse.class);
        assertThat(body.transactionId()).as("transactionId must be present in adjustment response").isNotNull();
        assertThat(body.recordedAt()).isCloseTo(clock.instant(), within(Duration.ofSeconds(2)));
        scenarioContext.setRequestedObjectId(body.transactionId().toString());
    }
}
