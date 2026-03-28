package com.github.jenkaby.bikerental.componenttest.steps.finance;

import com.github.jenkaby.bikerental.componenttest.context.ScenarioContext;
import com.github.jenkaby.bikerental.componenttest.steps.common.WebRequestSteps;
import com.github.jenkaby.bikerental.componenttest.transformer.shared.Aliases;
import com.github.jenkaby.bikerental.finance.PaymentMethod;
import com.github.jenkaby.bikerental.finance.web.command.dto.RecordDepositRequest;
import com.github.jenkaby.bikerental.finance.web.command.dto.RecordDepositResponse;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

@Slf4j
@RequiredArgsConstructor
public class DepositWebSteps {

    private final ScenarioContext scenarioContext;
    private final WebRequestSteps webRequestSteps;
    private final Clock clock;

    @Given("the deposit request is prepared with the following data")
    public void theDepositRequestIsPreparedWithTheFollowingData(RecordDepositRequest request) {
        log.info("Preparing deposit request: {}", request);
        scenarioContext.setRequestBody(request);
    }


    @Given("a {int} POST requests have been performed to deposit CASH with {int} amount")
    public void theDepositRequestIsPreparedWithTheFollowingData(int times, int amount) {
        List<RecordDepositRequest> requests = IntStream.range(0, times).mapToObj(i -> {
            return new RecordDepositRequest(
                    UUID.randomUUID(),
                    Aliases.getCustomerId("CUS2"), new BigDecimal(amount), PaymentMethod.CASH, Aliases.getOperatorId("OP1"));

        }).toList();
        webRequestSteps.parallelRequests(HttpMethod.POST, requests, "/api/finance/deposits", null);
    }

    @Then("the deposit response contains a transactionId")
    public void theDepositResponseContainsATransactionId() {
        RecordDepositResponse body = scenarioContext.getResponseBody(RecordDepositResponse.class);

        assertThat(body.transactionId())
                .as("transactionId must be present in deposit response").isNotNull();
        assertThat(body.recordedAt()).isCloseTo(clock.instant(), within(Duration.ofSeconds(2)));
        scenarioContext.setRequestedObjectId(body.transactionId().toString());
    }
}
