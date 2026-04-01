# Task 007: Component Tests — Withdrawal Feature

> **Applied Skill:** `spring-boot-java-cucumber` — `@DataTableType` transformer, `@ScenarioScope` web steps,
> reuse of existing `SubLedgerDbSteps`, `TransactionDbSteps`, `TransactionRecordDbSteps`, and generic
> `WebRequestSteps`; mirrors `DepositRequestTransformer` / `DepositWebSteps` / `transfers.feature` pattern.

## 1. Objective

Create the Cucumber component tests for the withdrawal feature, covering:

- **Scenario 1** (Scenario Outline): Successful cash withdrawal reduces wallet and credits the payout sub-ledger.
- **Scenario 2** (Scenario Outline): Duplicate submission is idempotent — only one journal entry is created.
- **Scenario 3** (Scenario Outline): Withdrawal rejected when amount exceeds available balance → `422`.

Three new files are required:
| # | File | Action |
|---|------|--------|
| 1 | `component-test/src/test/resources/features/finance/withdrawals.feature` | Create New File |
| 2 | `component-test/src/test/java/com/github/jenkaby/bikerental/componenttest/steps/finance/WithdrawalWebSteps.java` |
Create New File |
| 3 |
`component-test/src/test/java/com/github/jenkaby/bikerental/componenttest/transformer/finance/WithdrawalRequestTransformer.java` |
Create New File |

## 2. File to Modify / Create

### File 1 — `withdrawals.feature`

* **File Path:** `component-test/src/test/resources/features/finance/withdrawals.feature`
* **Action:** Create New File

**Code to Add/Replace:**

* **Location:** New file — full contents below.

```gherkin
Feature: Fund Withdrawal
  As a staff member
  I want to record a cash or cashless withdrawal for a customer
  So that the customer receives their available balance and the shop asset account is updated

  Background:
    Given the request header "Content-Type" is "application/vnd.bikerental.v1+json"
    And customers exist in the database with the following data
      | id   | phone       | firstName | lastName | email            | birthDate  |
      | CUS2 | +3706861555 | John      | Doe      | john@example.com | 1922-02-22 |
    And the following account records exist in db
      | id   | accountType | customerId |
      | ACC2 | CUSTOMER    | CUS2       |
    And the following sub-ledger records exist in db
      | id     | accountId | ledgerType      | balance | version | createdAt            | updatedAt            |
      | L_C_W2 | ACC2      | CUSTOMER_WALLET | 80.00   | 1       | 2026-03-27T00:00:00Z | 2026-03-27T00:00:00Z |
      | L_C_H2 | ACC2      | CUSTOMER_HOLD   | 20.00   | 1       | 2026-03-27T00:00:00Z | 2026-03-27T00:00:00Z |

  @ResetClock
  Scenario Outline: Successful partial cash withdrawal reduces customer wallet and credits the payout sub-ledger
    Given now is "<now>"
    And the withdrawal request is prepared with the following data
      | idempotencyKey   | customerId   | amount   | payoutMethod   | operatorId   |
      | <idempotencyKey> | <customerId> | <amount> | <payoutMethod> | <operatorId> |
    When a POST request has been made to "/api/finance/withdrawals" endpoint
    Then the response status is 201
    And the withdrawal response contains a transactionId
    And the following sub-ledger records were persisted in db
      | id     | accountId | ledgerType      | version | balance              |
      | L_C_W2 | ACC2      | CUSTOMER_WALLET | 2       | <expectedWallet>     |
      | L_C_H2 | ACC2      | CUSTOMER_HOLD   | 2       | 20.00                |
    And the following transactions were persisted in db
      | idempotencyKey   | customerId   | amount   | paymentMethod  | operatorId   | type       | recordedAt |
      | <idempotencyKey> | <customerId> | <amount> | <payoutMethod> | <operatorId> | WITHDRAWAL | <now>      |
    And the following transaction records were persisted in db
      | subLedger | ledgerType      | direction | amount   |
      | L_C_W2    | CUSTOMER_WALLET | DEBIT     | <amount> |
      | L_S_CASH  | CASH            | CREDIT    | <amount> |
    And there are only 2 transaction records in db
    Examples:
      | idempotencyKey | customerId | amount | payoutMethod | operatorId | now                  | expectedWallet |
      | IDK1           | CUS2       | 30.00  | CASH         | OP1        | 2026-03-28T10:00:00  | 50.00          |

  @ResetClock
  Scenario Outline: Duplicate withdrawal submission is idempotent
    Given now is "<now>"
    And the withdrawal request is prepared with the following data
      | idempotencyKey   | customerId   | amount   | payoutMethod | operatorId   |
      | <idempotencyKey> | <customerId> | <amount> | CASH         | <operatorId> |
    When a POST request has been made to "/api/finance/withdrawals" endpoint
    Then the response status is 201
    And the withdrawal response contains a transactionId
    Given the withdrawal request is prepared with the following data
      | idempotencyKey   | customerId   | amount   | payoutMethod | operatorId   |
      | <idempotencyKey> | <customerId> | <amount> | CASH         | <operatorId> |
    When a POST request has been made to "/api/finance/withdrawals" endpoint
    Then the response status is 201
    And the withdrawal response contains a transactionId
    And the following sub-ledger records were persisted in db
      | id     | accountId | ledgerType      | balance |
      | L_C_W2 | ACC2      | CUSTOMER_WALLET | 50.00   |
      | L_C_H2 | ACC2      | CUSTOMER_HOLD   | 20.00   |
    And there are only 1 transactions in db
    And there are only 2 transaction records in db
    Examples:
      | idempotencyKey | customerId | amount | operatorId | now                  |
      | IDK1           | CUS2       | 30.00  | OP1        | 2026-03-28T10:00:00  |

  Scenario Outline: Withdrawal rejected when amount exceeds available balance
    Given the withdrawal request is prepared with the following data
      | idempotencyKey   | customerId   | amount   | payoutMethod | operatorId   |
      | <idempotencyKey> | <customerId> | <amount> | CASH         | <operatorId> |
    When a POST request has been made to "/api/finance/withdrawals" endpoint
    Then the response status is 422
    Examples:
      | idempotencyKey | customerId | amount | operatorId |
      | IDK1           | CUS2       | 61.00  | OP1        |
```

---

### File 2 — `WithdrawalWebSteps.java`

* **File Path:**
  `component-test/src/test/java/com/github/jenkaby/bikerental/componenttest/steps/finance/WithdrawalWebSteps.java`
* **Action:** Create New File

**Code to Add/Replace:**

* **Location:** New file — full contents below.

```java
package com.github.jenkaby.bikerental.componenttest.steps.finance;

import com.github.jenkaby.bikerental.componenttest.context.ScenarioContext;
import com.github.jenkaby.bikerental.componenttest.steps.common.WebRequestSteps;
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
    private final WebRequestSteps webRequestSteps;
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
```

---

### File 3 — `WithdrawalRequestTransformer.java`

* **File Path:**
  `component-test/src/test/java/com/github/jenkaby/bikerental/componenttest/transformer/finance/WithdrawalRequestTransformer.java`
* **Action:** Create New File

**Code to Add/Replace:**

* **Location:** New file — full contents below.

```java
package com.github.jenkaby.bikerental.componenttest.transformer.finance;

import com.github.jenkaby.bikerental.componenttest.transformer.shared.Aliases;
import com.github.jenkaby.bikerental.finance.PaymentMethod;
import com.github.jenkaby.bikerental.finance.web.command.dto.RecordWithdrawalRequest;
import io.cucumber.java.DataTableType;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

public class WithdrawalRequestTransformer {

    @DataTableType
    public RecordWithdrawalRequest transform(Map<String, String> entry) {
        UUID idempotencyKey = Aliases.getUuid(entry.get("idempotencyKey"));
        UUID customerId = Aliases.getCustomerId(entry.get("customerId"));
        BigDecimal amount = new BigDecimal(entry.get("amount"));
        PaymentMethod payoutMethod = PaymentMethod.valueOf(entry.get("payoutMethod"));
        String operatorId = Aliases.getOperatorId(entry.get("operatorId"));
        return new RecordWithdrawalRequest(idempotencyKey, customerId, amount, payoutMethod, operatorId);
    }
}
```

## 4. Validation Steps

```bash
./gradlew :component-test:test "-Dspring.profiles.active=test"
```
