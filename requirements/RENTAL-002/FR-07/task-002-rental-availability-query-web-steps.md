# Task 002: Create `RentalAvailabilityQueryWebSteps`

> **Applied Skill:** `d:\Projects\private\bikerent\.github\skills\spring-boot-java-cucumber\SKILL.md` — Web step
> class pattern: `{Module}{Layer}Steps` naming; `@RequiredArgsConstructor`; `ScenarioContext.getResponseAsPage()`;
> `zipSatisfy` + `SoftAssertions` for list assertions; placed in `steps/rental/` next to `RentalWebSteps` as the
> canonical reference.

## 1. Objective

Create the step-definition class `RentalAvailabilityQueryWebSteps` in `steps/rental/`. It registers
the single new step:

```
Then the available equipment response only contains page of
```

This step reads the response body as a `Page<AvailableEquipmentResponse>`, sorts both the actual and
expected lists by `id`, and asserts field-by-field using `SoftAssertions`.

## 2. File to Modify / Create

* **File Path:**
  `component-test/src/test/java/com/github/jenkaby/bikerental/componenttest/steps/rental/RentalAvailabilityQueryWebSteps.java`
* **Action:** Create New File

## 3. Code Implementation

**Imports Required:** Already covered by the snippet below.

**Code to Add/Replace:**

* **Location:** New file — entire file content below.
* **Snippet:**

```java
package com.github.jenkaby.bikerental.componenttest.steps.rental;

import com.github.jenkaby.bikerental.componenttest.context.ScenarioContext;
import com.github.jenkaby.bikerental.rental.web.query.dto.AvailableEquipmentResponse;
import io.cucumber.java.en.Then;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

@Slf4j
@RequiredArgsConstructor
public class RentalAvailabilityQueryWebSteps {

    private static final Comparator<AvailableEquipmentResponse> BY_ID =
            Comparator.comparing(AvailableEquipmentResponse::id);

    private final ScenarioContext scenarioContext;

    @Then("the available equipment response only contains page of")
    public void theAvailableEquipmentResponseOnlyContainsPageOf(List<AvailableEquipmentResponse> expected) {
        var actual = scenarioContext.getResponseAsPage(AvailableEquipmentResponse.class).items()
                .stream()
                .sorted(BY_ID)
                .toList();
        log.info("Comparing available equipment response actual: {} with expected: {}", actual, expected);
        assertThat(actual)
                .as("Available equipment items list size")
                .hasSize(expected.size());
        var sortedExpected = expected.stream().sorted(BY_ID).toList();
        assertThat(actual).zipSatisfy(sortedExpected, (act, exp) ->
                assertSoftly(softly -> {
                    softly.assertThat(act.id()).as("Equipment ID").isEqualTo(exp.id());
                    softly.assertThat(act.uid()).as("UID").isEqualTo(exp.uid());
                    softly.assertThat(act.serialNumber()).as("Serial number").isEqualTo(exp.serialNumber());
                    softly.assertThat(act.typeSlug()).as("Type slug").isEqualTo(exp.typeSlug());
                    softly.assertThat(act.model()).as("Model").isEqualTo(exp.model());
                })
        );
    }
}
```

> **Key rules:**
> - `ScenarioContext.getResponseAsPage(AvailableEquipmentResponse.class)` — uses the existing
    > `getResponseAsPage` method; the `Page<T>` record exposes its contents via `.items()`.
> - Sort by `id` on both sides before `zipSatisfy` to avoid order-sensitivity.
> - `@RequiredArgsConstructor` — Spring injects `ScenarioContext` via constructor (it is `@ScenarioScope`).
> - No `@Component` annotation; Cucumber detects step-definition classes from the glue package automatically.

## 4. Validation Steps

skip