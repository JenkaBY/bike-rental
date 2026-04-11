# Task 001: Remove Component Test Artefacts

> **Applied Skill:** No dedicated skill file — follows project test conventions documented in `AGENTS.md` (Cucumber
> component tests) and `spring-boot-java-cucumber` skill.

## 1. Objective

Delete the `RecordPrepaymentRequestTransformer`, remove two prepayment step-definition methods from `RentalWebSteps`,
delete two prepayment-exercising scenarios from `rental.feature`, remove the `Record a payment` scenario from
`payments.feature`, and rename one misleadingly titled scenario to reflect the new hold-based guard. These changes are
done **before** the source classes they reference are deleted, ensuring the component-test module retains a compilable
state after its own cleanup.

---

## 2. Files to Modify / Create

* **File Path:**
  `component-test/src/test/java/com/github/jenkaby/bikerental/componenttest/transformer/finance/RecordPrepaymentRequestTransformer.java`
* **Action:** Delete file entirely.

---

* **File Path:**
  `component-test/src/test/java/com/github/jenkaby/bikerental/componenttest/steps/rental/RentalWebSteps.java`
* **Action:** Modify existing file — remove two step-definition methods.

---

* **File Path:** `component-test/src/test/resources/features/rental/rental.feature`
* **Action:** Modify existing file — delete two scenarios, rename one.

---

* **File Path:** `component-test/src/test/resources/features/finance/payments.feature`
* **Action:** Modify existing file — delete one scenario outline.

---

## 3. Code Implementation

### 3.1 Delete `RecordPrepaymentRequestTransformer.java`

Delete the file at:

```
component-test/src/test/java/com/github/jenkaby/bikerental/componenttest/transformer/finance/RecordPrepaymentRequestTransformer.java
```

No replacement is needed.

---

### 3.2 Modify `RentalWebSteps.java` — remove two step methods

**Location:** Remove the `@Given("the prepayment request is")` method. It appears immediately after the
`@Given("a rental request with the following data")` method.

**Remove this entire block** (including the blank line before it):

```java
    @Given("the prepayment request is")
    public void thePrepaymentRequestIs(RecordPrepaymentRequest request) {
        log.info("Preparing prepayment request: {}", request);
        scenarioContext.setRequestBody(request);
    }
```

**Location:** Remove the `@Then("the prepayment response contains")` method. It appears immediately after the
`thePrepaymentRequestIs` method removed above.

**Remove this entire block** (including the blank line before it):

```java
    @Then("the prepayment response contains")
    public void thePrepaymentRequestIs(PrepaymentResponse expected) {
        log.info("Preparing prepayment response: {}", expected);
        var actual = scenarioContext.getResponseBody(PrepaymentResponse.class);
        assertSoftly(softly -> {
            softly.assertThat(actual.paymentId()).as("Payment ID matches").isNotNull();
            softly.assertThat(actual.amount()).as("Amount matches").isEqualByComparingTo(expected.amount());
            softly.assertThat(actual.paymentMethod()).as("Payment method matches").isEqualTo(expected.paymentMethod());
            softly.assertThat(actual.receiptNumber()).as("Receipt number matches").isNotBlank();
            softly.assertThat(actual.createdAt()).as("Created at matches").isCloseTo(expected.createdAt(), within(5, ChronoUnit.SECONDS));
        });

        if (actual.paymentId() != null) {
            log.info("Saving paymentId {} to scenario context for later validation", actual.paymentId());
            scenarioContext.setRequestedObjectId(actual.paymentId().toString());
        }
    }
```

> **Note:** All remaining imports (including `com.github.jenkaby.bikerental.rental.web.command.dto.*`) are still
> used by other step definitions and must **not** be removed.

---

### 3.3 Modify `rental.feature` — delete two prepayment scenarios

**Location:** After the "Update rental - set duration" `Examples:` block and before the "Update rental - activate
rental" `Scenario Outline`.

**Remove this entire block** (two consecutive scenario outlines, lines starting with
`  Scenario Outline: Record prepayment for draft rental` through the closing `Examples:` table of the second
scenario):

```gherkin
  Scenario Outline: Record prepayment for draft rental
    Given now is "<now>"
    And a single rental exists in the database with the following data
      | id         | customerId | status | plannedDuration | createdAt | updatedAt |
      | <rentalId> | CUS1       | DRAFT  | 120             | <now>     | <now>     |
    And rental equipment exists in the database with the following data
      | rentalId   | equipmentId   | equipmentUid | tariffId | status   | startedAt           | expectedReturnAt    | estimatedCost | createdAt           | updatedAt           |
      | <rentalId> | <equipmentId> | BIKE-001     | 1        | ASSIGNED | 2026-02-10T08:00:00 | 2026-02-10T10:00:00 | 200.00        | 2026-02-10T08:00:00 | 2026-02-10T08:00:00 |
    And the prepayment request is
      | amount   | method   | operator |
      | <amount> | <method> | OP1      |
    When a POST request has been made to "/api/rentals/<rentalId>/prepayments" endpoint
    Then the response status is 201
    And the prepayment response contains
      | amount   | paymentMethod | createdAt |
      | <amount> | <method>      | <now>     |
    And the following payment received event was published
      | rentalId   | amount   | type       | receivedAt |
      | <rentalId> | <amount> | PREPAYMENT | <now>      |
    Examples:
      | rentalId | equipmentId | amount | method | now                 |
      | 10       | 1           | 200.00 | CASH   | 2026-02-10T10:15:30 |

  Scenario Outline: Reject prepayment when amount is below estimated cost
    Given a single rental exists in the database with the following data
      | id         | customerId | status | plannedDuration | createdAt           | updatedAt           |
      | <rentalId> | CUS1       | DRAFT  | 120             | 2026-02-06T10:00:00 | 2026-02-06T10:00:00 |
    And rental equipment exists in the database with the following data
      | rentalId   | equipmentId   | equipmentUid | tariffId | status   | startedAt           | expectedReturnAt    | estimatedCost | createdAt           | updatedAt           |
      | <rentalId> | <equipmentId> | BIKE-001     | 1        | ASSIGNED | 2026-02-10T08:00:00 | 2026-02-10T10:00:00 | 200.00        | 2026-02-10T08:00:00 | 2026-02-10T08:00:00 |
    And the prepayment request is
      | amount | method | operator |
      | 50.00  | CASH   | OP1      |
    When a POST request has been made to "/api/rentals/{requestedObjectId}/prepayments" endpoint with context
    Then the response status is 422
    And the response contains
      | path     | value                                                               |
      | $.title  | Insufficient prepayment                                             |
      | $.detail | Prepayment amount must be at least the estimated cost of the rental |
    Examples:
      | rentalId | equipmentId |
      | 10       | 1           |

```

---

### 3.4 Modify `rental.feature` — rename the misleadingly titled scenario

**Location:** The scenario titled `Attempt to activate rental without prepayment`. It tests the **new** `Hold required`
(409 Conflict) behaviour, not the old prepayment flow, so only the title is wrong.

**Replace:**

```gherkin
  Scenario: Attempt to activate rental without prepayment
```

**With:**

```gherkin
  Scenario: Attempt to activate rental without hold
```

---

### 3.5 Modify `payments.feature` — delete the `Record a payment` scenario

**Location:** The first scenario in `payments.feature`. It exercises `POST /api/payments` which is being removed.

**Remove this entire block** (from the blank line after the feature description through the closing `Examples:` table,
leaving the `Get payment by id` scenario as the first scenario):

```gherkin
  Scenario Outline: Record a payment
    Given the payment request is prepared with the following data
      | rentalId   | amount   | type   | method   | operator     |
      | <rentalId> | <amount> | <type> | <method> | <operatorId> |
    When a POST request has been made to "/api/payments" endpoint
    Then the response status is 201
    And the record payment response is valid
    And the following payment record was persisted in db
      | rentalId   | amount   | type   | method   | operator     |
      | <rentalId> | <amount> | <type> | <method> | <operatorId> |
    And the following payment received event was published
      | rentalId   | amount   | type   |
      | <rentalId> | <amount> | <type> |
    Examples:
      | rentalId | amount | type               | method        | operatorId |
      | 1001     | 10.00  | PREPAYMENT         | CASH          | OP1        |
      |          | 20.00  | ADDITIONAL_PAYMENT | BANK_TRANSFER | OP2        |

```

---

## 4. Validation Steps

```bash
./gradlew :component-test:compileTestJava "-Dspring.profiles.active=test"
```

Expected result: `BUILD SUCCESSFUL` with zero compilation errors in the `component-test` module.
