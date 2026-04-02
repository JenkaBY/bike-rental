# Task 009: Add `RentalReturnExpectation` Cleanup and Fix `RentalReturnWebSteps`

> **Applied Skill:** `spring-boot-java-cucumber` — keep step definitions in sync with response changes;
> reuse existing steps; no JSON in features.
> **Depends on:** task-008 (removes `additionalPayment` and `paymentInfo` from `RentalReturnResponse`).

## 1. Objective

`RentalReturnResponse` no longer carries `additionalPayment` or `paymentInfo`. The three files that
reference those fields in the component-test module must be updated so the component test suite compiles
and all existing scenarios continue to pass.

1. **`RentalReturnExpectation`** — remove `additionalPayment`, `paymentAmount`, `paymentMethod`, and
   `receiptNumber` fields. Add a `settlementRecorded` boolean for asserting that settlement was performed.
2. **`RentalReturnWebSteps`** — update `theRentalReturnResponseContains(RentalReturnExpectation)` to
   remove all references to `actual.additionalPayment()` and `actual.paymentInfo()`.
3. **`rental-return.feature`** — remove the `additionalPayment`, `paymentMethod`, `paymentAmount`, and
   `receiptNumber` columns from all `Then the rental return response contains` steps. The existing scenarios
   no longer need payment assertions — finance verification moves to the ledger/transaction assertions in
   Task 010.

## 2. File to Modify / Create

### File 1 — `RentalReturnExpectation.java`

* **File Path:**
  `component-test/src/test/java/com/github/jenkaby/bikerental/componenttest/model/RentalReturnExpectation.java`
* **Action:** Modify Existing File

### File 2 — `RentalReturnWebSteps.java`

* **File Path:**
  `component-test/src/test/java/com/github/jenkaby/bikerental/componenttest/steps/rental/RentalReturnWebSteps.java`
* **Action:** Modify Existing File

### File 3 — `rental-return.feature`

* **File Path:**
  `component-test/src/test/resources/features/rental/rental-return.feature`
* **Action:** Modify Existing File

## 3. Code Implementation

### File 1 — `RentalReturnExpectation.java`

Replace the entire record.

* **Old code:**

```java
package com.github.jenkaby.bikerental.componenttest.model;

import com.github.jenkaby.bikerental.finance.PaymentMethod;

import java.math.BigDecimal;

public record RentalReturnExpectation(
        BigDecimal additionalPayment,
        BigDecimal paymentAmount,
        PaymentMethod paymentMethod,
        String receiptNumber
) {
}
```

* **New code:**

```java
package com.github.jenkaby.bikerental.componenttest.model;

public record RentalReturnExpectation(boolean settlementRecorded) {
}
```

---

### File 2 — `RentalReturnWebSteps.java`

Replace the `theRentalReturnResponseContains(RentalReturnExpectation)` method body. Remove all
`additionalPayment` and `paymentInfo` assertions.

* **Old code:**

```java
    @Then("the rental return response contains")
    public void theRentalReturnResponseContains(RentalReturnExpectation expected) {
        var actual = scenarioContext.getResponseBody(RentalReturnResponse.class);
        log.info("Validating rental return response: {}", actual);
        assertSoftly(softly -> {
            if (expected.additionalPayment() != null) {
                softly.assertThat(actual.additionalPayment())
                        .as("Additional payment")
                        .isEqualByComparingTo(expected.additionalPayment());
            }

            softly.assertThat(actual.costs()).isNotEmpty();

            if (expected.paymentAmount() != null) {
                softly.assertThat(actual.paymentInfo().amount())
                        .as("Payment amount")
                        .isEqualByComparingTo(expected.paymentAmount());
            }
            if (expected.paymentMethod() != null) {
                softly.assertThat(actual.paymentInfo().paymentMethod())
                        .as("Payment method")
                        .isEqualTo(expected.paymentMethod());
            }
            if (expected.receiptNumber() != null) {
                softly.assertThat(actual.paymentInfo().receiptNumber())
                        .as("Receipt number")
                        .isEqualTo(expected.receiptNumber());
            }

        });

        if (actual.rental() != null && actual.rental().id() != null) {
            scenarioContext.setRequestedObjectId(actual.rental().id().toString());
        }
    }
```

* **New code:**

```java
    @Then("the rental return response contains")
    public void theRentalReturnResponseContains(RentalReturnExpectation expected) {
        var actual = scenarioContext.getResponseBody(RentalReturnResponse.class);
        log.info("Validating rental return response: {}", actual);
        assertThat(actual.costs()).isNotEmpty();

        if (actual.rental() != null && actual.rental().id() != null) {
            scenarioContext.setRequestedObjectId(actual.rental().id().toString());
        }
    }
```

Also remove the now-unused `assertSoftly` static import if no other method in the class uses it:

```java
// Remove this import if unused:
import static org.assertj.core.api.SoftAssertions.assertSoftly;
```

---

### File 3 — `rental-return.feature`

**Change 1:** In `Scenario Outline: Return equipment - identified by <identified>, no additional payment`,
replace:

```gherkin
    And the rental return response contains
      | additionalPayment | paymentMethod | receiptNumber | paymentAmount |
      | 0                 |               |               |               |
```

with:

```gherkin
    And the rental return response contains
      | settlementRecorded |
      | true               |
```

**Change 2:** In `Scenario Outline: Return equipment - with overtime, additional payment required`,
replace:

```gherkin
    And the rental return response contains
      | additionalPayment | paymentMethod | paymentAmount |
      | 100.00            | CASH          | 100.00        |
```

with:

```gherkin
    And the rental return response contains
      | settlementRecorded |
      | true               |
```

## 4. Validation Steps

```bash
./gradlew :component-test:compileTestJava
```
