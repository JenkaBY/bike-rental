# Task 014: Write Component Test for Fund Deposit (Cucumber BDD)

> **Applied Skill:** `spring-boot-java-cucumber` — Gherkin feature file; step definitions split by infrastructure
> (`WebSteps` for HTTP, `DbSteps` for DB assertions); follows exact same pattern as `payments.feature` and
> `customer-account-creation.feature`.

## 1. Objective

Add a Cucumber component test covering the three key scenarios from the FR acceptance criteria (Scenarios 1, 3,
and 4). These tests run against a live database and validate the full stack — HTTP → service → JPA → DB.

> **Scope note:** Scenario 2 (card terminal deposit) is structurally identical to Scenario 1 and is omitted to
> avoid duplication; it can be added as a data-driven extension of the happy-path scenario if desired.

## 2. Files to Create

| # | File Path | Action |
|---|-----------|--------|
| 1 | `component-test/src/test/resources/features/finance/deposit.feature` | Create New File |
| 2 | `component-test/src/test/java/com/github/jenkaby/bikerental/componenttest/steps/finance/DepositWebSteps.java` | Create New File |
| 3 | `component-test/src/test/java/com/github/jenkaby/bikerental/componenttest/steps/finance/DepositDbSteps.java` | Create New File |
| 4 | `component-test/src/test/java/com/github/jenkaby/bikerental/componenttest/transformer/DepositRequestTransformer.java` | Create New File |

## 3. Code Implementation

### File 1 — `deposit.feature`

```gherkin
Feature: Fund Deposit
  As a staff member
  I want to record a customer fund deposit at the counter
  So that the customer's wallet balance is increased

  Background:
    Given the request header "Content-Type" is "application/vnd.bikerental.v1+json"

  @run
  Scenario: Successful cash deposit increases customer wallet balance
    Given a customer is registered with phone "+79991234568" firstName "Anna" lastName "Ivanova"
    When staff records a cash deposit of 50.00 for the customer with operator "operator-1"
    Then the response status is 201
    And the deposit response contains a transactionId
    And the customer wallet balance is increased by 50.00 in db
    And a transaction record exists in db with type "DEPOSIT" and paymentMethod "CASH" and operatorId "operator-1"

  Scenario: Deposit rejected for unknown customer
    When a deposit request is submitted for unknown customerId "00000000-0000-0000-0000-000000000000" with amount 50.00 and paymentMethod "CASH" and operator "operator-1"
    Then the response status is 404

  Scenario: Deposit rejected for zero amount
    Given a customer is registered with phone "+79991234569" firstName "Boris" lastName "Sidorov"
    When a deposit request is submitted for the customer with amount 0.00 and paymentMethod "CASH" and operator "operator-1"
    Then the response status is 400
```

### File 2 — `DepositWebSteps.java`

```java
package com.github.jenkaby.bikerental.componenttest.steps.finance;

import com.github.jenkaby.bikerental.componenttest.context.ScenarioContext;
import com.github.jenkaby.bikerental.finance.PaymentMethod;
import com.github.jenkaby.bikerental.finance.web.command.dto.RecordDepositRequest;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@RequiredArgsConstructor
public class DepositWebSteps {

    private final ScenarioContext scenarioContext;
    private final TestRestTemplate restClient;
    @LocalServerPort
    private final int port;

    @When("staff records a cash deposit of {bigdecimal} for the customer with operator {string}")
    public void staffRecordsCashDeposit(BigDecimal amount, String operatorId) {
        String customerId = scenarioContext.extractFromLastResponse("$.id");
        RecordDepositRequest request = new RecordDepositRequest(
                UUID.fromString(customerId), amount, PaymentMethod.CASH, operatorId);
        ResponseEntity<String> response = restClient.postForEntity(
                "http://localhost:" + port + "/api/finance/deposits",
                buildHttpEntity(request), String.class);
        scenarioContext.setLastResponse(response);
        log.info("Deposit response status: {}", response.getStatusCode());
    }

    @When("a deposit request is submitted for unknown customerId {string} with amount {bigdecimal} and paymentMethod {string} and operator {string}")
    public void depositForUnknownCustomer(String customerId, BigDecimal amount, String paymentMethod, String operatorId) {
        RecordDepositRequest request = new RecordDepositRequest(
                UUID.fromString(customerId), amount, PaymentMethod.valueOf(paymentMethod), operatorId);
        ResponseEntity<String> response = restClient.postForEntity(
                "http://localhost:" + port + "/api/finance/deposits",
                buildHttpEntity(request), String.class);
        scenarioContext.setLastResponse(response);
    }

    @When("a deposit request is submitted for the customer with amount {bigdecimal} and paymentMethod {string} and operator {string}")
    public void depositWithAmount(BigDecimal amount, String paymentMethod, String operatorId) {
        String customerId = scenarioContext.extractFromLastResponse("$.id");
        RecordDepositRequest request = new RecordDepositRequest(
                UUID.fromString(customerId), amount, PaymentMethod.valueOf(paymentMethod), operatorId);
        ResponseEntity<String> response = restClient.postForEntity(
                "http://localhost:" + port + "/api/finance/deposits",
                buildHttpEntity(request), String.class);
        scenarioContext.setLastResponse(response);
    }

    @Then("the deposit response contains a transactionId")
    public void theDepositResponseContainsATransactionId() {
        String transactionId = scenarioContext.extractFromLastResponse("$.transactionId");
        assertThat(transactionId).as("transactionId must be present in deposit response").isNotNull();
        scenarioContext.store("depositTransactionId", transactionId);
    }

    private HttpEntity<RecordDepositRequest> buildHttpEntity(RecordDepositRequest request) {
        HttpHeaders headers = new HttpHeaders();
        scenarioContext.getRequestHeaders().forEach((k, v) -> headers.put(k, v));
        headers.setContentType(MediaType.valueOf("application/vnd.bikerental.v1+json"));
        return new HttpEntity<>(request, headers);
    }
}
```

### File 3 — `DepositDbSteps.java`

```java
package com.github.jenkaby.bikerental.componenttest.steps.finance;

import com.github.jenkaby.bikerental.componenttest.context.ScenarioContext;
import com.github.jenkaby.bikerental.finance.domain.model.LedgerType;
import com.github.jenkaby.bikerental.finance.infrastructure.persistence.entity.TransactionJpaEntity;
import com.github.jenkaby.bikerental.finance.infrastructure.persistence.repository.AccountJpaRepository;
import com.github.jenkaby.bikerental.finance.infrastructure.persistence.repository.TransactionJpaRepository;
import io.cucumber.java.en.Then;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

@Slf4j
@RequiredArgsConstructor
public class DepositDbSteps {

    private final ScenarioContext scenarioContext;
    private final AccountJpaRepository accountJpaRepository;
    private final TransactionJpaRepository transactionJpaRepository;

    @Then("the customer wallet balance is increased by {bigdecimal} in db")
    public void theCustomerWalletBalanceIsIncreasedBy(BigDecimal expectedIncrease) {
        String customerId = scenarioContext.extractFromLastCustomerResponse("$.id");
        var account = accountJpaRepository.findByCustomerId(UUID.fromString(customerId))
                .orElseThrow(() -> new AssertionError("Customer finance account not found for customerId=" + customerId));

        var wallet = account.getSubLedgers().stream()
                .filter(sl -> sl.getLedgerType() == LedgerType.CUSTOMER_WALLET)
                .findFirst()
                .orElseThrow(() -> new AssertionError("CUSTOMER_WALLET sub-ledger not found"));

        assertThat(wallet.getBalance())
                .as("CUSTOMER_WALLET balance should equal deposit amount")
                .isEqualByComparingTo(expectedIncrease);
    }

    @Then("a transaction record exists in db with type {string} and paymentMethod {string} and operatorId {string}")
    public void aTransactionRecordExistsInDb(String type, String paymentMethod, String operatorId) {
        String transactionId = scenarioContext.retrieve("depositTransactionId", String.class);
        TransactionJpaEntity tx = transactionJpaRepository.findById(UUID.fromString(transactionId))
                .orElseThrow(() -> new AssertionError("Transaction not found in db for id=" + transactionId));

        assertSoftly(softly -> {
            softly.assertThat(tx.getTransactionType().name()).as("transaction type").isEqualTo(type);
            softly.assertThat(tx.getPaymentMethod().name()).as("payment method").isEqualTo(paymentMethod);
            softly.assertThat(tx.getOperatorId()).as("operatorId").isEqualTo(operatorId);
            softly.assertThat(tx.getRecords()).as("must have exactly 2 transaction records").hasSize(2);
        });
    }
}
```

### File 4 — `DepositRequestTransformer.java`

> This transformer is not required for the step implementations above (steps build requests inline), but is
> provided for completeness and future extension of the feature file with data-table-based scenarios.

```java
package com.github.jenkaby.bikerental.componenttest.transformer;

import com.github.jenkaby.bikerental.finance.PaymentMethod;
import com.github.jenkaby.bikerental.finance.web.command.dto.RecordDepositRequest;
import io.cucumber.java.DataTableType;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

public class DepositRequestTransformer {

    @DataTableType
    public RecordDepositRequest transform(Map<String, String> entry) {
        return new RecordDepositRequest(
                UUID.fromString(entry.get("customerId")),
                new BigDecimal(entry.get("amount")),
                PaymentMethod.valueOf(entry.get("paymentMethod")),
                entry.get("operatorId")
        );
    }
}
```

> **Note on `ScenarioContext` helper methods used:**
> - `scenarioContext.extractFromLastResponse(jsonPath)` — reads a JSONPath value from the last HTTP response body.
>   This method may or may not already exist; if absent, add a thin helper to `ScenarioContext` that uses
>   `JsonPath.read(body, path)`.
> - `scenarioContext.store(key, value)` / `scenarioContext.retrieve(key, type)` — generic key-value storage for
>   cross-step data sharing within a scenario. Add these methods to `ScenarioContext` if not present.
> - `scenarioContext.extractFromLastCustomerResponse(jsonPath)` — reads from the customer registration response.
>   This may require storing the customer registration response separately in the context (e.g. under
>   `"customerResponse"` key) in the customer registration step definition.

## 4. Validation Steps

```bash
./gradlew :component-test:test "-Dspring.profiles.active=test"
```

The `deposit.feature` scenarios tagged `@run` must pass. The two unhappy-path scenarios without `@run` are
executed in the full suite run.
