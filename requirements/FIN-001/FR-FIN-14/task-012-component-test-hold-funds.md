# Task 012: Component Test — Hold Funds on Rental Creation

> **Applied Skill:** `spring-boot-java-cucumber` — Component tests use Cucumber BDD feature files with existing step
> definitions from `SubLedgerDbSteps`, `TransactionDbSteps`, and `RentalWebSteps`. Happy paths only; negative / validation
> scenarios belong in WebMvc tests.

## 1. Objective

Add two BDD scenarios to `rental.feature` that verify the hold-funds integration:

1. **Sufficient balance** — rental is created and a `HOLD` transaction is persisted with the correct sub-ledger
   mutations.
2. **Insufficient balance** — `POST /api/rentals` returns `422` and no rental or journal entry is persisted.

## 2. File to Modify / Create (Part A — feature file)

* **File Path:** `component-test/src/test/resources/features/rental/rental.feature`
* **Action:** Modify Existing File

### Scenario to Add

* **Location:** At the **end** of the file, after the last existing scenario.

**Add the following tag annotation to the entire feature (if not already present):**

> Do NOT add `@ReinitializeSystemLedgers` to the individual scenarios — the Background already sets up customers and
> equipment; the system ledgers are re-initialised by the tag on the feature-level if used. Check whether any existing
> rental scenarios that use finance operations need this.

```gherkin
  Scenario: Create rental holds funds from customer wallet — sufficient balance
    Given the following account records exist in db
      | id   | accountType | customerId |
      | ACC1 | CUSTOMER    | CUS1       |
    And the following sub-ledger records exist in db
      | id     | accountId | ledgerType      | balance | version | createdAt            | updatedAt            |
      | L_C_W1 | ACC1      | CUSTOMER_WALLET | 200.00  | 1       | 2026-01-01T00:00:00Z | 2026-01-01T00:00:00Z |
      | L_C_H1 | ACC1      | CUSTOMER_HOLD   | 0.00    | 1       | 2026-01-01T00:00:00Z | 2026-01-01T00:00:00Z |
    And a rental request with the following data
      | customerId | equipmentIds | duration |
      | CUS1       | 1,3          | PT2H     |
    When a POST request has been made to "/api/rentals" endpoint
    Then the response status is 201
    And the following sub-ledger records were persisted in db
      | id     | accountId | ledgerType      | balance |
      | L_C_W1 | ACC1      | CUSTOMER_WALLET | -20.00  |
      | L_C_H1 | ACC1      | CUSTOMER_HOLD   | 220.00  |
    And the following transactions were persisted in db
      | customerId | amount | type | paymentMethod |
      | CUS1       | 220.00 | HOLD | null          |

  @ReinitializeSystemLedgers
  Scenario: Create rental rejected when customer wallet has insufficient balance
    Given the following account records exist in db
      | id   | accountType | customerId |
      | ACC1 | CUSTOMER    | CUS1       |
    And the following sub-ledger records exist in db
      | id     | accountId | ledgerType      | balance | version | createdAt            | updatedAt            |
      | L_C_W1 | ACC1      | CUSTOMER_WALLET | 50.00   | 1       | 2026-01-01T00:00:00Z | 2026-01-01T00:00:00Z |
      | L_C_H1 | ACC1      | CUSTOMER_HOLD   | 0.00    | 1       | 2026-01-01T00:00:00Z | 2026-01-01T00:00:00Z |
    And a rental request with the following data
      | customerId | equipmentIds | duration |
      | CUS1       | 1,3          | PT2H     |
    When a POST request has been made to "/api/rentals" endpoint
    Then the response status is 422
    And the response contains errorCode "rental.insufficient_funds"
    And there are/is only 0 transactions in db
```

> **NOTE on expected balance for `CUSTOMER_WALLET`:** The planned cost for equipment 1 (bicycle, 2 h, tariff 1) is
> 200.00 and for equipment 3 (helmet, 2 h, tariff 3) is 20.00, giving `totalPlannedCost = 220.00`. After the hold,
`CUSTOMER_WALLET = 200.00 − 220.00 = −20.00`. Wait — this would make the wallet negative, which violates the balance
> guard. Adjust the initial wallet balance to `300.00` in the "sufficient balance" scenario so the post-hold balance is
`80.00`. Use the corrected values below:

**Corrected "sufficient balance" sub-ledger setup:**

Replace the initial balance for `L_C_W1` with `300.00`, and expected post-hold balance with `80.00`:

```gherkin
    And the following sub-ledger records exist in db
      | id     | accountId | ledgerType      | balance | version | createdAt            | updatedAt            |
      | L_C_W1 | ACC1      | CUSTOMER_WALLET | 300.00  | 1       | 2026-01-01T00:00:00Z | 2026-01-01T00:00:00Z |
      | L_C_H1 | ACC1      | CUSTOMER_HOLD   | 0.00    | 1       | 2026-01-01T00:00:00Z | 2026-01-01T00:00:00Z |
    ...
    And the following sub-ledger records were persisted in db
      | id     | accountId | ledgerType      | balance |
      | L_C_W1 | ACC1      | CUSTOMER_WALLET | 80.00   |
      | L_C_H1 | ACC1      | CUSTOMER_HOLD   | 220.00  |
```

---

## 3. File to Modify / Create (Part B — check for missing step definition)

Before running the tests, verify that the following step is defined in an existing step-definitions class:

```
Then the response contains errorCode "<value>"
```

Search for this step in `component-test/src/test/java/...`. If it does not exist, add it to  
`component-test/src/test/java/com/github/jenkaby/bikerental/componenttest/steps/shared/CommonWebSteps.java`  
(or the equivalent shared steps class):

```java
@Then("the response contains errorCode {string}")
public void theResponseContainsErrorCode(String expectedErrorCode) {
    var body = scenarioContext.getResponseBody();
    assertThat(body).as("errorCode field").contains(expectedErrorCode);
}
```

> Use `scenarioContext.getResponseBody()` (raw String) if a JSON-path helper is not already available. Alternatively,
> use `scenarioContext.getResponseAs(Map.class)` and assert on the `errorCode` key — follow the pattern used in the most
> similar test in the same class.

## 4. Validation Steps

```bash
./gradlew :component-test:test "-Dspring.profiles.active=test"
```
