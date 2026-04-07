# Task 007: Component Test — Account Balances Feature

> **Applied Skill:** `spring-boot-java-cucumber` — Cucumber BDD component test; happy-path scenarios only;
> reuses existing `WebRequestSteps` for HTTP calls and existing `AccountDbSteps` / `SubLedgerDbSteps` steps
> for DB setup; new `BalancesWebSteps` + `CustomerAccountBalancesResponseTransformer` for response assertions.

## 1. Objective

Add two happy-path component test scenarios for `GET /api/finance/customers/{customerId}/balances`:

1. Customer with a non-zero wallet and hold balance returns the correct figures and a non-null `lastUpdatedAt`.
2. Customer with no transactions (zero balances) returns zeros and a non-null `lastUpdatedAt`.

A `CustomerAccountBalancesResponseTransformer` converts the Gherkin DataTable into a typed object. A new
`BalancesWebSteps` class performs the typed assertions. No negative scenarios (404, 400) are added here — those
are covered by the WebMvc test (Task 006). Existing account and sub-ledger table schemas are unchanged.

---

## 2. Files to Create

### File 1 — Cucumber Feature

* **File Path:**
  `component-test/src/test/resources/features/finance/account-balances.feature`
* **Action:** Create New File

**Full file content:**

```gherkin
@ReinitializeSystemLedgers
Feature: Customer account balance retrieval
  As a staff member
  I want to retrieve a customer's current wallet and hold balances
  So that I can advise the customer on available funds before any financial operation

  Background:
    Given the request header "Content-Type" is "application/vnd.bikerental.v1+json"
    And customers exist in the database with the following data
      | id   | phone       | firstName | lastName | email            | birthDate  |
      | CUS2 | +3706861555 | John      | Doe      | john@example.com | 1922-02-22 |
    And the following account records exist in db
      | id   | accountType | customerId |
      | ACC2 | CUSTOMER    | CUS2       |

  Scenario: Retrieve balances for a customer with wallet funds and an active hold
    Given the following sub-ledger records exist in db
      | id     | accountId | ledgerType      | balance | version | createdAt            | updatedAt            |
      | L_C_W2 | ACC2      | CUSTOMER_WALLET | 120.00  | 2       | 2026-03-27T00:00:00Z | 2026-04-07T10:30:00Z |
      | L_C_H2 | ACC2      | CUSTOMER_HOLD   | 30.00   | 2       | 2026-03-27T00:00:00Z | 2026-04-07T10:30:00Z |
    When a GET request has been made to "/api/finance/customers/{customerId}/balances" endpoint with
      | {customerId} | CUS2 |
    Then the response status is 200
    And the balances response contains
      | walletBalance | holdBalance |
      | 120.00        | 30.00       |
    And the balances response lastUpdatedAt is not null

  Scenario: Retrieve balances for a customer with no transactions returns zero balances
    Given the following sub-ledger records exist in db
      | id     | accountId | ledgerType      | balance | version | createdAt            | updatedAt            |
      | L_C_W2 | ACC2      | CUSTOMER_WALLET | 0.00    | 1       | 2026-03-27T00:00:00Z | 2026-03-27T00:00:00Z |
      | L_C_H2 | ACC2      | CUSTOMER_HOLD   | 0.00    | 1       | 2026-03-27T00:00:00Z | 2026-03-27T00:00:00Z |
    When a GET request has been made to "/api/finance/customers/{customerId}/balances" endpoint with
      | {customerId} | CUS2 |
    Then the response status is 200
    And the balances response contains
      | walletBalance | holdBalance |
      | 0.00          | 0.00        |
    And the balances response lastUpdatedAt is not null
```

---

### File 2 — DataTable Transformer

* **File Path:**
  `component-test/src/test/java/com/github/jenkaby/bikerental/componenttest/transformer/finance/CustomerAccountBalancesResponseTransformer.java`
* **Action:** Create New File

**Full file content:**

```java
package com.github.jenkaby.bikerental.componenttest.transformer.finance;

import com.github.jenkaby.bikerental.finance.web.query.dto.CustomerAccountBalancesResponse;
import io.cucumber.java.DataTableType;

import java.math.BigDecimal;
import java.util.Map;

public class CustomerAccountBalancesResponseTransformer {

    @DataTableType
    public CustomerAccountBalancesResponse transform(Map<String, String> row) {
        return new CustomerAccountBalancesResponse(
                new BigDecimal(row.get("walletBalance")),
                new BigDecimal(row.get("holdBalance")),
                null
        );
    }
}
```

**Notes:**

- `lastUpdatedAt` is set to `null` in the transformer because it is asserted separately in its own step
  (`"the balances response lastUpdatedAt is not null"`) rather than driven from the DataTable.
- Cucumber discovers `@DataTableType` methods by scanning the test classpath — no manual registration needed.

---

### File 3 — Step Definitions

* **File Path:**
  `component-test/src/test/java/com/github/jenkaby/bikerental/componenttest/steps/finance/BalancesWebSteps.java`
* **Action:** Create New File

**Full file content:**

```java
package com.github.jenkaby.bikerental.componenttest.steps.finance;

import com.github.jenkaby.bikerental.componenttest.context.ScenarioContext;
import com.github.jenkaby.bikerental.finance.web.query.dto.CustomerAccountBalancesResponse;
import io.cucumber.java.en.Then;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.SoftAssertions;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@RequiredArgsConstructor
public class BalancesWebSteps {

    private final ScenarioContext scenarioContext;

    @Then("the balances response contains")
    public void theBalancesResponseContains(CustomerAccountBalancesResponse expected) {
        CustomerAccountBalancesResponse body = scenarioContext.getResponseBody(CustomerAccountBalancesResponse.class);
        log.info("Balances response: {}", body);

        var softly = new SoftAssertions();
        softly.assertThat(body.walletBalance())
                .as("walletBalance")
                .isEqualByComparingTo(expected.walletBalance());
        softly.assertThat(body.holdBalance())
                .as("holdBalance")
                .isEqualByComparingTo(expected.holdBalance());
        softly.assertAll();
    }

    @Then("the balances response lastUpdatedAt is not null")
    public void theBalancesResponseLastUpdatedAtIsNotNull() {
        CustomerAccountBalancesResponse body = scenarioContext.getResponseBody(CustomerAccountBalancesResponse.class);
        assertThat(body.lastUpdatedAt()).as("lastUpdatedAt must not be null").isNotNull();
    }
}
```

**Notes on step design:**

- The step method parameter type `CustomerAccountBalancesResponse` is resolved by Cucumber via the
  `@DataTableType` method in `CustomerAccountBalancesResponseTransformer` — no `DataTable` or `@Transpose`
  import is needed in the step class.
- `isEqualByComparingTo` is used for `BigDecimal` comparison (ignores scale differences) per project
  conventions.
- The `lastUpdatedAt` assertion is a separate step so it can be appended independently to any scenario.
- `DbSteps.TABLE_TO_TRUNCATE` does **not** need to change — `finance_sub_ledgers` and `finance_accounts` are
  already cleaned up by the `deleteFromTableWhere` calls in `DbSteps.truncateDb()`.

---

## 4. Validation Steps

```bash
./gradlew :component-test:test "-Dspring.profiles.active=test"
```

Expected: both new scenarios pass; no pre-existing scenarios regress.
