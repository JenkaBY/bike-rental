<task_file_template>

# Task 007: Component-test coverage for version exposure and increment

> **Applied Skill:** `spring-boot-java-cucumber` — happy-path business behavior only, AssertJ,
> state via `ScenarioContext`, reusable steps in the step class; `component-tests` rule — no
> request-validation cases here. Covers `fr.md` Acceptance Criteria 1 (version = 0 on create) and 2
> (version increments on PUT update). Criterion 3 (HTTP 409) is out of scope — handled by the
> pre-existing global `CoreExceptionHandlerAdvice` and needs no scenario (per design.md §6).

## 1. Objective

Add two reusable assertion steps to `RentalWebSteps` and extend the existing
"Update rental with all required fields (tariff autoselect)" scenario in `rental.feature` to assert
that a freshly created draft has `version = 0` and that after a `PUT` update the `version` is greater
than `0`.

## 2. File to Modify / Create

Two files are modified in this task.

* **File Path A:** `component-test/src/test/java/com/github/jenkaby/bikerental/componenttest/steps/rental/RentalWebSteps.java`
* **File Path B:** `component-test/src/test/resources/features/rental/rental.feature`
* **Action:** Modify Existing File (both)

## 3. Code Implementation

### File A — RentalWebSteps.java

**Imports Required:**

No new imports are needed. `RentalResponse`, `@Then`, `assertThat` and the `scenarioContext` field are
already present.

**Code to Add/Replace:**

* **Location:** Add the two new step methods immediately AFTER the existing
  `theRentalResponseOnlyContains(RentalResponse expectedRental)` method (the one annotated
  `@Then("the rental response only contains")`) and BEFORE the
  `@Then("the rental response only contains rental equipments")` method.
* **Snippet:**

```java
    @Then("the rental response version is {long}")
    public void theRentalResponseVersionIs(long expectedVersion) {
        var actualRental = scenarioContext.getResponseBody(RentalResponse.class);
        assertThat(actualRental.version())
                .as("Rental version")
                .isEqualTo(expectedVersion);
    }

    @Then("the rental response version is greater than {long}")
    public void theRentalResponseVersionIsGreaterThan(long threshold) {
        var actualRental = scenarioContext.getResponseBody(RentalResponse.class);
        assertThat(actualRental.version())
                .as("Rental version")
                .isGreaterThan(threshold);
    }
```

### File B — rental.feature

**Code to Add/Replace:**

* **Location:** In the `Scenario Outline: Update rental with all required fields (tariff autoselect)`
  block. Add ONE assertion line after the first `And the rental response only contains` table (the
  `DRAFT`-status assertion following the initial `POST /api/rentals/draft`), and ONE assertion line
  after the second `And the rental response only contains` table (the one following the successful
  `PUT`).

Replace the beginning of the scenario:

```gherkin
  Scenario Outline: Update rental with all required fields (tariff autoselect)
    Given a POST request has been made to "/api/rentals/draft" endpoint
    Then the response status is 201
    And the rental response only contains
      | status |
      | DRAFT  |
#    Update draft rental
```

With:

```gherkin
  Scenario Outline: Update rental with all required fields (tariff autoselect)
    Given a POST request has been made to "/api/rentals/draft" endpoint
    Then the response status is 201
    And the rental response only contains
      | status |
      | DRAFT  |
    And the rental response version is 0
#    Update draft rental
```

Then replace the post-`PUT` assertion block:

```gherkin
    When a PUT request has been made to "/api/rentals/{requestedObjectId}" endpoint with context
    Then the response status is 200
    And the rental response only contains
      | customerId   | status | plannedDuration   | estimatedCost |
      | <customerId> | DRAFT  | <plannedDuration> | 17.00         |
    And the rental response only contains rental equipments
```

With:

```gherkin
    When a PUT request has been made to "/api/rentals/{requestedObjectId}" endpoint with context
    Then the response status is 200
    And the rental response only contains
      | customerId   | status | plannedDuration   | estimatedCost |
      | <customerId> | DRAFT  | <plannedDuration> | 17.00         |
    And the rental response version is greater than 0
    And the rental response only contains rental equipments
```

> Do NOT change the `Examples:` table or any other scenario.

## 4. Validation Steps

Execute the following commands to ensure this task was successful. Do NOT run the full application
server. Assume the database is already up and accepting connections.

```bash
./gradlew :component-test:test "-Dspring.profiles.active=test"
```

</task_file_template>
