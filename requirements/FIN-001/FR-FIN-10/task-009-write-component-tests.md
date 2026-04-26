# Task 009: Write Component Tests for Transaction History Endpoint

> **Applied Skill:** `spring-boot-java-cucumber` — Cucumber BDD component tests; happy paths, no validation negative
> cases

## 1. Objective

Add a `transaction-history.feature` file and a companion `TransactionHistoryWebSteps.java` step definition covering
Scenarios 1, 3, 4, 6, 7, 8, and 9 from FR-FIN-10. Also add new transaction aliases to `Aliases` and extend
`Aliases.java` with `TX1`–`TX6` identifiers.

## 2. File to Modify / Create

### 2a.

* **File Path:** `component-test/src/test/resources/features/finance/transaction-history.feature`
* **Action:** Create New File

### 2b.

* **File Path:**
  `component-test/src/test/java/com/github/jenkaby/bikerental/componenttest/transformer/TransactionEntryResponseTransformer.java`
* **Action:** Create New File

### 2c.

* **File Path:**
  `component-test/src/test/java/com/github/jenkaby/bikerental/componenttest/steps/finance/TransactionHistoryWebSteps.java`
* **Action:** Create New File

### 2d.

* **File Path:**
  `component-test/src/test/java/com/github/jenkaby/bikerental/componenttest/transformer/shared/Aliases.java`
* **Action:** Modify Existing File

## 3. Code Implementation

### 2a — `transaction-history.feature`

Pagination assertions use the generic `And the response contains` step (from `WebRequestSteps`) with `path`/`value`
table. The `Page<T>` JSON structure serialises as `{"items":[...], "totalItems": N,
"pageRequest": {"size": S, "page": P, "sortBy": null}}`.

```gherkin
Feature: Customer transaction history retrieval
  As a staff member
  I want to retrieve a paginated list of all financial entries for a customer
  So that I can audit all financial activity for that customer

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
      | L_C_W2 | ACC2      | CUSTOMER_WALLET | 200.00  | 5       | 2026-01-01T00:00:00Z | 2026-04-07T10:00:00Z |
      | L_C_H2 | ACC2      | CUSTOMER_HOLD   | 0.00    | 1       | 2026-01-01T00:00:00Z | 2026-01-01T00:00:00Z |

  Scenario: Paginated query returns entries in reverse-chronological order
    Given the following transaction records exist in db
      | id  | type    | paymentMethod | amount | customerId | operatorId | sourceType | sourceId | recordedAt           | idempotencyKey |
      | TX1 | DEPOSIT | CASH          | 50.00  | CUS2       | OP1        |            |          | 2026-01-10T10:00:00Z | IDK1           |
      | TX2 | DEPOSIT | CASH          | 60.00  | CUS2       | OP1        |            |          | 2026-02-15T10:00:00Z | IDK2           |
      | TX3 | DEPOSIT | CASH          | 70.00  | CUS2       | OP1        |            |          | 2026-03-20T10:00:00Z | IDK3           |
    When a GET request has been made to "/api/finance/customers/{customerId}/transactions" endpoint with
      | {customerId} |
      | CUS2         |
    Then the response status is 200
    And the response contains
      | path                | value |
      | $.totalItems        | 3     |
      | $.pageRequest.page  | 0     |
      | $.pageRequest.size  | 20    |
    And the transaction history entries are ordered by recordedAt descending

  Scenario: Query for customer with no transactions returns empty result
    When a GET request has been made to "/api/finance/customers/{customerId}/transactions" endpoint with
      | {customerId} |
      | CUS2         |
    Then the response status is 200
    And the response contains
      | path                | value |
      | $.totalItems        | 0     |
      | $.pageRequest.page  | 0     |
      | $.pageRequest.size  | 20    |

  Scenario: Query rejected for unknown customer returns 404
    When a GET request has been made to "/api/finance/customers/{customerId}/transactions" endpoint with
      | {customerId}                         |
      | 00000000-0000-0000-0000-000000000099 |
    Then the response status is 404

  Scenario: Filter by date range returns only entries within the range
    Given the following transaction records exist in db
      | id  | type    | paymentMethod | amount | customerId | operatorId | sourceType | sourceId | recordedAt           | idempotencyKey |
      | TX1 | DEPOSIT | CASH          | 50.00  | CUS2       | OP1        |            |          | 2026-01-10T10:00:00Z | IDK1           |
      | TX2 | DEPOSIT | CASH          | 60.00  | CUS2       | OP1        |            |          | 2026-02-15T10:00:00Z | IDK2           |
      | TX3 | DEPOSIT | CASH          | 70.00  | CUS2       | OP1        |            |          | 2026-03-20T10:00:00Z | IDK3           |
    When a GET request has been made to "/api/finance/customers/{customerId}/transactions" endpoint with query parameters
      | {customerId} | fromDate   | toDate     |
      | CUS2         | 2026-02-01 | 2026-02-28 |
    Then the response status is 200
    And the response contains
      | path                | value |
      | $.totalItems        | 1     |
      | $.pageRequest.page  | 0     |
      | $.pageRequest.size  | 20    |

  Scenario: Filter by sourceType returns only matching entries
    Given the following transaction records exist in db
      | id  | type    | paymentMethod | amount | customerId | operatorId | sourceType | sourceId | recordedAt           | idempotencyKey |
      | TX1 | DEPOSIT | CASH          | 50.00  | CUS2       | OP1        |            |          | 2026-01-10T10:00:00Z | IDK1           |
      | TX2 | HOLD    | CASH          | 30.00  | CUS2       | OP1        | RENTAL     | RENT1    | 2026-02-15T10:00:00Z | IDK2           |
    When a GET request has been made to "/api/finance/customers/{customerId}/transactions" endpoint with query parameters
      | {customerId} | sourceType |
      | CUS2         | RENTAL     |
    Then the response status is 200
    And the response contains
      | path                | value |
      | $.totalItems        | 1     |
      | $.pageRequest.page  | 0     |
      | $.pageRequest.size  | 20    |
    And the transaction history response only contains entries of
      | subLedger     | amount | direction | type | sourceType | sourceId |
      | CUSTOMER_HOLD | 30.00  | DEBIT     | HOLD | RENTAL     | RENT1    |

  Scenario: Filter by sourceId returns only entries linked to that source
    Given the following transaction records exist in db
      | id  | type    | paymentMethod | amount | customerId | operatorId | sourceType | sourceId | recordedAt           | idempotencyKey |
      | TX1 | HOLD    | CASH          | 30.00  | CUS2       | OP1        | RENTAL     | RENT1    | 2026-01-10T10:00:00Z | IDK1           |
      | TX2 | HOLD    | CASH          | 40.00  | CUS2       | OP1        | RENTAL     | RENT2    | 2026-02-15T10:00:00Z | IDK2           |
    When a GET request has been made to "/api/finance/customers/{customerId}/transactions" endpoint with query parameters
      | {customerId} | sourceId |
      | CUS2         | RENT1    |
    Then the response status is 200
    And the response contains
      | path                | value |
      | $.totalItems        | 1     |
      | $.pageRequest.page  | 0     |
      | $.pageRequest.size  | 20    |
    And the transaction history response only contains entries of
      | subLedger     | amount | direction | type | sourceType | sourceId |
      | CUSTOMER_HOLD | 30.00  | DEBIT     | HOLD | RENTAL     | RENT1    |

  Scenario: Combined filters apply AND logic
    Given the following transaction records exist in db
      | id  | type    | paymentMethod | amount | customerId | operatorId | sourceType | sourceId | recordedAt           | idempotencyKey |
      | TX1 | DEPOSIT | CASH          | 50.00  | CUS2       | OP1        |            |          | 2026-01-10T10:00:00Z | IDK1           |
      | TX2 | HOLD    | CASH          | 30.00  | CUS2       | OP1        | RENTAL     | RENT1    | 2026-02-15T10:00:00Z | IDK2           |
      | TX3 | HOLD    | CASH          | 30.00  | CUS2       | OP1        | RENTAL     | RENT2    | 2026-03-20T10:00:00Z | IDK3           |
    When a GET request has been made to "/api/finance/customers/{customerId}/transactions" endpoint with query parameters
      | {customerId} | fromDate   | toDate     | sourceType |
      | CUS2         | 2026-02-01 | 2026-02-28 | RENTAL     |
    Then the response status is 200
    And the response contains
      | path                | value |
      | $.totalItems        | 1     |
      | $.pageRequest.page  | 0     |
      | $.pageRequest.size  | 20    |
    And the transaction history response only contains entries of
      | subLedger     | amount | direction | type | sourceType | sourceId |
      | CUSTOMER_HOLD | 30.00  | DEBIT     | HOLD | RENTAL     | RENT1    |
```

---

### 2b — `TransactionEntryResponseTransformer.java`

`@DataTableType` transformer that maps a Cucumber DataTable row to a `TransactionEntryResponse`. All enum-typed
columns are plain strings in the response record, so no conversion is needed for them. Uses
`Aliases.getValueOrDefault()` for `sourceId` so `RENT1`/`RENT2` placeholder names resolve to their actual values.

```java
package com.github.jenkaby.bikerental.componenttest.transformer;

import com.github.jenkaby.bikerental.componenttest.transformer.shared.Aliases;
import com.github.jenkaby.bikerental.finance.web.query.dto.CustomerTransactionResponse;
import io.cucumber.java.DataTableType;

import java.util.Map;
import java.util.Optional;

public class TransactionEntryResponseTransformer {

    @DataTableType
    public TransactionEntryResponse transactionResponse(Map<String, String> entry) {
      var sourceIdRaw = DataTableHelper.getStringOrNull(entry, "sourceId");
      var sourceId = Optional.ofNullable(sourceIdRaw)
        .map(Aliases::getValueOrDefault)
        .orElse(null);
      var customerIdRaw = DataTableHelper.getStringOrNull(entry, "customerId");
      var customerId = Optional.ofNullable(customerIdRaw)
        .map(Aliases::getValueOrDefault)
        .orElse(null);

        return new TransactionEntryResponse(
        DataTableHelper.getStringOrNull(entry, "subLedger"),
        customerId,
        DataTableHelper.toBigDecimal(entry, "amount"),
        DataTableHelper.getStringOrNull(entry, "direction"),
        DataTableHelper.getStringOrNull(entry, "type"),
        DataTableHelper.toInstant(entry, "recordedAt"),
        DataTableHelper.getStringOrNull(entry, "paymentMethod"),
        DataTableHelper.getStringOrNull(entry, "reason"),
        DataTableHelper.getStringOrNull(entry, "sourceType"),
        sourceId
        );
    }
}
```

---

### 2c — `TransactionHistoryWebSteps.java`

Contains only:

- `theTransactionHistoryEntriesAreOrderedByRecordedAtDescending` — reads `$.items` (matches `Page<T>` serialisation)
- `theTransactionHistoryResponseOnlyContainsEntriesOf` — validates items list against expected entries

The pagination assertion (`totalItems`, `pageRequest.page`, `pageRequest.size`) is handled by the generic
`And the response contains` step from `WebRequestSteps` — **no custom step needed for it here**.

```java
package com.github.jenkaby.bikerental.componenttest.steps.finance;

import com.github.jenkaby.bikerental.componenttest.context.ScenarioContext;
import com.github.jenkaby.bikerental.finance.web.query.dto.CustomerTransactionResponse;
import io.cucumber.java.en.Then;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.SoftAssertions;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@RequiredArgsConstructor
public class TransactionHistoryWebSteps {

    private static final Comparator<TransactionEntryResponse> COMPARING_BY_RECORDED_AT =
            Comparator.comparing(e -> e.recordedAt().toString());

    private final ScenarioContext scenarioContext;

    @Then("the transaction history response only contains entries of")
    public void theTransactionHistoryResponseOnlyContainsEntriesOf(List<TransactionEntryResponse> expectedEntries) {
        var actualEntries = scenarioContext.getResponseAsPage(TransactionEntryResponse.class).items()
                .stream().sorted(COMPARING_BY_RECORDED_AT).toList();
        log.info("Comparing transaction history actual: {} with expected: {}", actualEntries, expectedEntries);
        assertThat(actualEntries)
                .as("Transaction history list size")
                .hasSize(expectedEntries.size());
        assertThat(actualEntries).zipSatisfy(
                expectedEntries.stream().sorted(COMPARING_BY_RECORDED_AT).toList(),
                this::validateEntry);
    }

    @Then("the transaction history entries are ordered by recordedAt descending")
    public void theTransactionHistoryEntriesAreOrderedByRecordedAtDescending() {
        var body = scenarioContext.getResponseBody(Map.class);
        @SuppressWarnings("unchecked")
        var items = (List<Map<String, Object>>) body.get("items");

        assertThat(items).isNotEmpty();

        for (int i = 0; i < items.size() - 1; i++) {
            var current = (String) items.get(i).get("recordedAt");
            var next = (String) items.get(i + 1).get("recordedAt");
            assertThat(current).as("entry[%d].recordedAt >= entry[%d].recordedAt", i, i + 1)
                    .isGreaterThanOrEqualTo(next);
        }
    }

    private void validateEntry(TransactionEntryResponse actual, TransactionEntryResponse expected) {
        log.info("Comparing transaction entry actual: {} with expected: {}", actual, expected);
        var softly = new SoftAssertions();
        softly.assertThat(actual.subLedger()).as("subLedger").isEqualTo(expected.subLedger());
        softly.assertThat(actual.customerId()).as("customerId").isEqualTo(expected.customerId());
        softly.assertThat(actual.amount()).as("amount").isEqualByComparingTo(expected.amount());
        softly.assertThat(actual.direction()).as("direction").isEqualTo(expected.direction());
        softly.assertThat(actual.type()).as("type").isEqualTo(expected.type());
        if (expected.paymentMethod() != null) {
            softly.assertThat(actual.paymentMethod()).as("paymentMethod").isEqualTo(expected.paymentMethod());
        }
        if (expected.reason() != null) {
            softly.assertThat(actual.reason()).as("reason").isEqualTo(expected.reason());
        }
        if (expected.sourceType() != null) {
            softly.assertThat(actual.sourceType()).as("sourceType").isEqualTo(expected.sourceType());
        }
        if (expected.sourceId() != null) {
            softly.assertThat(actual.sourceId()).as("sourceId").isEqualTo(expected.sourceId());
        }
        softly.assertAll();
    }
}
```

---

### 2d — `Aliases.java`

**Location:** In the static initializer block `static { ALIASES = ... }`, after the last existing `ALIASES.put` line
for idempotency keys (`IDK3`).

**Snippet to add:**

```java
        // Transaction ids group starts with 66666666-
        ALIASES.put("TX1", "66666666-6666-6666-6666-666666666661");
        ALIASES.put("TX2", "66666666-6666-6666-6666-666666666662");
        ALIASES.put("TX3", "66666666-6666-6666-6666-666666666663");
        ALIASES.put("TX4", "66666666-6666-6666-6666-666666666664");
        ALIASES.put("TX5", "66666666-6666-6666-6666-666666666665");
        ALIASES.put("TX6", "66666666-6666-6666-6666-666666666666");
        // Source / Rental aliases — stored as Long string because rental.id is Long
        ALIASES.put("RENT1", "1001");
        ALIASES.put("RENT2", "1002");
```

> **Note on `TransactionDbSteps@Given`:** The step `@Given("the following transactions exist in db")` is already
> declared in `TransactionDbSteps`. Verify it exists and reuse it — do NOT duplicate the step definition.

> **Note on `And the response contains`:** The generic `path`/`value` step is declared in `WebRequestSteps`.
> No custom pagination step is needed in `TransactionHistoryWebSteps`.

## 4. Validation Steps

```bash
./gradlew :component-test:test "-Dspring.profiles.active=test"
```

All new scenarios in `transaction-history.feature` must pass. If `@Given the following transactions exist in db` is
missing from `TransactionDbSteps`, add it there before running this task.
