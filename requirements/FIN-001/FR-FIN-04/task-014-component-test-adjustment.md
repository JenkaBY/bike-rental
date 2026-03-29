# Task 014: Component Tests — Manual Balance Adjustment

> **Applied Skill:** `spring-boot-java-cucumber` — feature file per module, transformer per request DTO,
> module-scoped web steps, reuse of existing `SubLedgerDbSteps` / `TransactionDbSteps` /
> `TransactionRecordDbSteps` for state assertions.

## 1. Objective

Cover the happy paths and the insufficient-balance failure path for `POST /api/finance/adjustments`:

1. **Top-up** — positive amount increases `CUSTOMER_WALLET` balance; `ADJUSTMENT` sub-ledger charged;
   one `ADJUSTMENT` transaction with two records persisted.
2. **Deduction** — negative amount decreases `CUSTOMER_WALLET` balance (sufficient funds);
   same journal structure as top-up but directions reversed.
3. **Insufficient balance** — deduction amount exceeds wallet balance → `422 Unprocessable Content`
   returned; no balance mutation, no transaction record written.

## 2. Files to Modify / Create

| Action | Path                                                                                                                                |
|--------|-------------------------------------------------------------------------------------------------------------------------------------|
| Create | `component-test/src/test/resources/features/finance/adjustments.feature`                                                            |
| Create | `component-test/src/test/java/com/github/jenkaby/bikerental/componenttest/transformer/finance/AdjustmentRequestTransformer.java`    |
| Create | `component-test/src/test/java/com/github/jenkaby/bikerental/componenttest/steps/finance/AdjustmentWebSteps.java`                    |
| Modify | `component-test/src/test/java/com/github/jenkaby/bikerental/componenttest/transformer/finance/TransactionJpaEntityTransformer.java` |
| Modify | `component-test/src/test/java/com/github/jenkaby/bikerental/componenttest/steps/finance/TransactionDbSteps.java`                    |

## 3. Code Implementation

---

### 3a. `adjustments.feature`

```gherkin
Feature: Manual balance adjustments
  As an administrator
  I want to apply manual balance corrections to a customer's wallet
  So that I can compensate for system errors or apply penalty deductions

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
      | L_C_W2 | ACC2      | CUSTOMER_WALLET | 40.00   | 1       | 2026-03-27T00:00:00Z | 2026-03-27T00:00:00Z |
      | L_C_H2 | ACC2      | CUSTOMER_HOLD   | 0.00    | 1       | 2026-03-27T00:00:00Z | 2026-03-27T00:00:00Z |

  @ResetClock
  Scenario Outline: Successful top-up increases customer wallet balance
    Given the adjustment request is prepared with the following data
      | customerId | amount   | reason                      | operatorId | idempotencyKey |
      | CUS2       | <amount> | Compensation for system error | OP1       | IDK1           |
    When a POST request is performed to "/api/finance/adjustments"
    Then the response status is 201
    And the adjustment response contains a transactionId
    And the following sub-ledger record(s) were/was persisted in db
      | id     | accountId | ledgerType      | balance    |
      | L_C_W2 | ACC2      | CUSTOMER_WALLET | <walletBal>|
      | L_S_ADJ |          | ADJUSTMENT      | <adjBal>   |
    And the following transaction(s) were/was persisted in db
      | customerId | type       | paymentMethod     | amount   | operatorId | reason                      |
      | CUS2       | ADJUSTMENT | INTERNAL_TRANSFER | <amount> | OP1        | Compensation for system error |

    Examples:
      | amount | walletBal | adjBal |
      | 10.00  | 50.00     | 10.00  |

  @ResetClock
  Scenario Outline: Successful deduction decreases customer wallet balance
    Given the adjustment request is prepared with the following data
      | customerId | amount    | reason           | operatorId | idempotencyKey |
      | CUS2       | <amount>  | Penalty deduction | OP1        | IDK1           |
    When a POST request is performed to "/api/finance/adjustments"
    Then the response status is 201
    And the adjustment response contains a transactionId
    And the following sub-ledger record(s) were/was persisted in db
      | id     | accountId | ledgerType      | balance     |
      | L_C_W2 | ACC2      | CUSTOMER_WALLET | <walletBal> |
      | L_S_ADJ |          | ADJUSTMENT      | <adjBal>    |
    And the following transaction(s) were/was persisted in db
      | customerId | type       | paymentMethod     | amount   | operatorId | reason            |
      | CUS2       | ADJUSTMENT | INTERNAL_TRANSFER | <absAmt> | OP1        | Penalty deduction |

    Examples:
      | amount | absAmt | walletBal | adjBal |
      | -15.00 | 15.00  | 25.00     | 15.00  |

  Scenario: Deduction rejected when wallet balance is insufficient
    Given the adjustment request is prepared with the following data
      | customerId | amount  | reason            | operatorId | idempotencyKey |
      | CUS2       | -50.00  | Penalty deduction | OP1        | IDK1           |
    When a POST request is performed to "/api/finance/adjustments"
    Then the response status is 422
    And there are/is only 0 transactions in db
    And the following sub-ledger record(s) were/was persisted in db
      | id     | accountId | ledgerType      | balance |
      | L_C_W2 | ACC2      | CUSTOMER_WALLET | 40.00   |
```

---

### 3b. `AdjustmentRequestTransformer.java`

```java
package com.github.jenkaby.bikerental.componenttest.transformer.finance;

import com.github.jenkaby.bikerental.componenttest.transformer.DataTableHelper;
import com.github.jenkaby.bikerental.componenttest.transformer.shared.Aliases;
import com.github.jenkaby.bikerental.finance.web.command.dto.AdjustmentRequest;
import io.cucumber.java.DataTableType;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

public class AdjustmentRequestTransformer {

    @DataTableType
    public AdjustmentRequest transform(Map<String, String> entry) {
        UUID customerId = Aliases.getCustomerId(entry.get("customerId"));
        BigDecimal amount = DataTableHelper.toBigDecimal(entry, "amount");
        String reason = entry.get("reason");
        String operatorId = Aliases.getOperatorId(entry.get("operatorId"));
        UUID idempotencyKey = Aliases.getUuid(entry.get("idempotencyKey"));
        return new AdjustmentRequest(customerId, amount, reason, operatorId, idempotencyKey);
    }
}
```

---

### 3c. `AdjustmentWebSteps.java`

```java
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
```

---

### 3d. Modify `TransactionJpaEntityTransformer.java`

`reason` is not yet read from the datatable. Add the following line **after** `entity.setRecordedAt(recordedAt);`
and **before** the closing of the `transform` method:

```java
entity.setReason(DataTableHelper.getStringOrNull(entry, "reason"));
```

---

### 3e. Modify `TransactionDbSteps.java`

The `compareSingleElement` method does not yet assert `reason`. Add the following assertion **after**
`softly.assertThat(actual.getRecords()).as("Records").isNotEmpty();` and **before** `softly.assertAll();`:

```java
if (exp.getReason() != null) {
    softly.assertThat(actual.getReason()).as("Reason").isEqualTo(exp.getReason());
}
```

## 4. Validation Steps

```bash
./gradlew :component-test:test "-Dspring.profiles.active=test,docker"
```
