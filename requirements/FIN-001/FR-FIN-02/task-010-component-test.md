# Task 010: Component Tests — Customer Finance Account Creation

> **Applied Skill:** `spring-boot-java-cucumber/SKILL.md` — Feature file per bounded-context integration scenario;
> `FinanceDbSteps` for DB assertions following `PaymentDbSteps` pattern; add tables to `DbSteps.TABLE_TO_TRUNCATE`;
> use `AccountJpaRepository` to read actual persisted state; use `SoftAssertions` for multi-field assertions;
> use `isEqualByComparingTo` for `BigDecimal`; no JSON in step definitions.
> `spring-boot-data-ddd/SKILL.md` — Use corresponding `jpaRepository` to retrieve entities in test assertions.

## 1. Objective

Add a Cucumber component test covering the happy path from FR-FIN-02 Scenario 1: registering a new customer
triggers automatic creation of a Customer Finance Account with `CUSTOMER_WALLET` and `CUSTOMER_HOLD` sub-ledgers at
balance zero.

The task involves four changes:

1. Update `DbSteps.TABLE_TO_TRUNCATE` to include `finance_sub_ledgers` and `finance_accounts` so customer account
   rows are cleaned between scenarios.
2. Create `FinanceAccountDbSteps.java` with a Then-step that verifies the persisted account and sub-ledger rows.
3. Create the feature file `customer-account-creation.feature`.

## 2. Files to Modify / Create

### File 1 — Update DbSteps TABLE_TO_TRUNCATE

* **File Path:**
  `component-test/src/test/java/com/github/jenkaby/bikerental/componenttest/steps/common/hook/DbSteps.java`
* **Action:** Modify Existing File

### File 2 — New FinanceAccountDbSteps

* **File Path:**
  `component-test/src/test/java/com/github/jenkaby/bikerental/componenttest/steps/finance/FinanceAccountDbSteps.java`
* **Action:** Create New File

### File 3 — New Feature File

* **File Path:**
  `component-test/src/test/resources/features/finance/customer-account-creation.feature`
* **Action:** Create New File

---

## 3. Code Implementation

### 3.1 Update DbSteps.TABLE_TO_TRUNCATE

**Location:** Inside `DbSteps.java`, replace the `TABLE_TO_TRUNCATE` list field.

**Old code:**

```java
    private static final List<String> TABLE_TO_TRUNCATE = List.of(
//            order is important due to foreign key constraints
            "event_publication", // from modulith event_api
            "event_publication_archive", // from modulith event_api
            "equipment_status_transition_rules",
            "customers",
            "equipments",
            "equipment_statuses",
            "equipment_types",
            "tariffs",
            "tariffs_v2",
            "payments",
            "rental_equipments",
            "rentals"
    );
```

**New code:**

```java
    private static final List<String> TABLE_TO_TRUNCATE = List.of(
//            order is important due to foreign key constraints
            "event_publication", // from modulith event_api
            "event_publication_archive", // from modulith event_api
            "equipment_status_transition_rules",
            "customers",
            "equipments",
            "equipment_statuses",
            "equipment_types",
            "tariffs",
            "tariffs_v2",
            "payments",
            "rental_equipments",
            "rentals",
            "finance_sub_ledgers",
            "finance_accounts"
    );
```

**Note:** `finance_sub_ledgers` must appear before `finance_accounts` because of the FK constraint
`fk_finance_sub_ledgers_finance_accounts`. The JdbcTestUtils helper deletes rows in the listed order.

---

### 3.2 Create FinanceAccountDbSteps.java

**Imports Required:**

```java
import com.github.jenkaby.bikerental.componenttest.context.ScenarioContext;
import com.github.jenkaby.bikerental.finance.domain.model.AccountType;
import com.github.jenkaby.bikerental.finance.domain.model.LedgerType;
import com.github.jenkaby.bikerental.finance.infrastructure.persistence.entity.AccountJpaEntity;
import com.github.jenkaby.bikerental.finance.infrastructure.persistence.repository.AccountJpaRepository;
import io.cucumber.java.en.Then;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
```

**Full file content:**

```java
package com.github.jenkaby.bikerental.componenttest.steps.finance;

import com.github.jenkaby.bikerental.componenttest.context.ScenarioContext;
import com.github.jenkaby.bikerental.finance.domain.model.AccountType;
import com.github.jenkaby.bikerental.finance.domain.model.LedgerType;
import com.github.jenkaby.bikerental.finance.infrastructure.persistence.entity.AccountJpaEntity;
import com.github.jenkaby.bikerental.finance.infrastructure.persistence.repository.AccountJpaRepository;
import io.cucumber.java.en.Then;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

@Slf4j
@RequiredArgsConstructor
public class FinanceAccountDbSteps {

    private final ScenarioContext scenarioContext;
    private final AccountJpaRepository accountJpaRepository;

    @Then("a customer finance account is created in db for the registered customer")
    public void aCustomerFinanceAccountIsCreatedInDbForTheRegisteredCustomer() {
        String customerId = scenarioContext.getStringResponseBody()
                .contains("\"id\"") ? extractCustomerIdFromResponse() : null;

        assertThat(customerId).as("Customer ID in response must not be null").isNotNull();

        UUID customerUuid = UUID.fromString(customerId);

        AccountJpaEntity account = accountJpaRepository.findByCustomerId(customerUuid)
                .orElseThrow(() -> new AssertionError(
                        "Expected a Customer Finance Account for customerId=" + customerUuid + " but none was found"));

        assertSoftly(softly -> {
            softly.assertThat(account.getAccountType())
                    .as("Account type must be CUSTOMER")
                    .isEqualTo(AccountType.CUSTOMER);
            softly.assertThat(account.getCustomerId())
                    .as("Account must reference the registered customer")
                    .isEqualTo(customerUuid);
            softly.assertThat(account.getId())
                    .as("Account id must not be null")
                    .isNotNull();
            softly.assertThat(account.getSubLedgers())
                    .as("Account must have exactly 2 sub-ledgers")
                    .hasSize(2);
        });

        var wallet = account.getSubLedgers().stream()
                .filter(sl -> sl.getLedgerType() == LedgerType.CUSTOMER_WALLET)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Expected CUSTOMER_WALLET sub-ledger not found"));

        var hold = account.getSubLedgers().stream()
                .filter(sl -> sl.getLedgerType() == LedgerType.CUSTOMER_HOLD)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Expected CUSTOMER_HOLD sub-ledger not found"));

        assertSoftly(softly -> {
            softly.assertThat(wallet.getBalance())
                    .as("CUSTOMER_WALLET balance must be zero")
                    .isEqualByComparingTo(BigDecimal.ZERO);
            softly.assertThat(hold.getBalance())
                    .as("CUSTOMER_HOLD balance must be zero")
                    .isEqualByComparingTo(BigDecimal.ZERO);
        });
    }

    private String extractCustomerIdFromResponse() {
        var body = scenarioContext.getStringResponseBody();
        int idIndex = body.indexOf("\"id\":\"");
        if (idIndex == -1) {
            return null;
        }
        int start = idIndex + 6;
        int end = body.indexOf("\"", start);
        return body.substring(start, end);
    }
}
```

**Note:** The `extractCustomerIdFromResponse()` helper reads the `$.id` from the raw JSON string returned by the
customer creation endpoint (HTTP 201 body). This avoids importing a full ObjectMapper deserialization step since only
the UUID string is needed.

---

### 3.3 Create customer-account-creation.feature

```gherkin
Feature: Customer Finance Account Creation
  As a system
  I want to automatically create a finance account for every new customer upon registration
  So that the customer can immediately make deposits and participate in rental transactions

  Background:
    Given the request header "Content-Type" is "application/vnd.bikerental.v1+json"

  Scenario: New customer registration creates a finance account with zero-balance sub-ledgers
    Given a customer request with the following data
      | phone        | firstName | lastName |
      | +79991234567 | Ivan      | Petrov   |
    When a POST request has been made to "/api/customers" endpoint
    Then the response status is 201
    And a customer finance account is created in db for the registered customer
```

## 4. Validation Steps

```bash
./gradlew :component-test:test "-Dspring.profiles.active=test"
```
