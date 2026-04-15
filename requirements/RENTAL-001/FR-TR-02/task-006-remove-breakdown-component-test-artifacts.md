# Task 006: Remove V1 Breakdown Artifacts from Component Test Infrastructure

> **Applied Skill:** `spring-boot-java-cucumber` — Component test step definitions must stay aligned with the current
> API contract; dead steps and transformers that reference deleted DTO fields must be removed.

## 1. Objective

`RentalReturnResponse.CostBreakdown` no longer exists (deleted in Task 004). Remove all component test code that
references it: delete `RentalReturnCostBreakdownTransformer.java` and strip the breakdown-assertion step (and its
private helpers) from `RentalReturnWebSteps.java`.

## 2. Files to Modify / Delete

### File A — Delete

* **File Path:**
  `component-test/src/test/java/com/github/jenkaby/bikerental/componenttest/transformer/RentalReturnCostBreakdownTransformer.java`
* **Action:** Delete this file entirely (it will no longer compile).

### File B — Modify

* **File Path:**
  `component-test/src/test/java/com/github/jenkaby/bikerental/componenttest/steps/rental/RentalReturnWebSteps.java`
* **Action:** Modify Existing File

## 3. Code Implementation for File B

### Step A — Remove import

**Remove** the following import:

```java
import org.assertj.core.api.SoftAssertions;
```

Also **remove** this import if it is present (it refers to `CostBreakdown` from the deleted record):

```java
import com.github.jenkaby.bikerental.rental.web.command.dto.RentalReturnResponse;
```

> **Note:** `RentalReturnResponse` is still used by the class (for `actual.settlement()` and `actual.rental()`), so
> only remove the import if the class compiles without it after the code changes below. In practice, the import must
> remain — only the `CostBreakdown` inner type is gone.

### Step B — Remove the comparator field

* **Location:** Remove the static field at the top of the class:

**Remove:**

```java
    public static final Comparator<RentalReturnResponse.CostBreakdown> DEFAULT_COMPARING_COST_BREAKDOWN = Comparator.comparing(RentalReturnResponse.CostBreakdown::equipmentId);
```

Also remove the unused `import java.util.Comparator;` if the class no longer references `Comparator` elsewhere.

### Step C — Remove the breakdown step method and its private helper

* **Location:** Remove both methods from `RentalReturnWebSteps` that deal with cost breakdown.

**Remove:**

```java
    @Then("the rental return response contains the following break down costs")
    public void theRentalReturnResponseContainsTheFollowingBreakDownCosts(List<RentalReturnResponse.CostBreakdown> expected) {
        var actual = scenarioContext.getResponseBody(RentalReturnResponse.class);
        log.info("Validating rental return response cost breakdown: {}", actual);
        var actualCosts = actual.costs();

        assertThat(actualCosts)
                .as("Cost breakdown list size")
                .hasSize(expected.size());

        actualCosts.sort(DEFAULT_COMPARING_COST_BREAKDOWN);
        expected.sort(DEFAULT_COMPARING_COST_BREAKDOWN);

        assertThat(actualCosts).zipSatisfy(expected, this::assertCostBreakdown);
    }

    private void assertCostBreakdown(RentalReturnResponse.CostBreakdown actual, RentalReturnResponse.CostBreakdown expected) {
        var softly = new SoftAssertions();

        if (expected.baseCost() != null) {
            softly.assertThat(actual.baseCost())
                    .as("Base cost")
                    .isEqualByComparingTo(expected.baseCost());
        }
        if (expected.overtimeCost() != null) {
            softly.assertThat(actual.overtimeCost())
                    .as("Overtime cost")
                    .isEqualByComparingTo(expected.overtimeCost());
        }
        if (expected.totalCost() != null) {
            softly.assertThat(actual.totalCost())
                    .as("Final total cost")
                    .isEqualByComparingTo(expected.totalCost());
        }
        softly.assertThat(actual.actualMinutes())
                .as("Actual minutes")
                .isEqualTo(expected.actualMinutes());

        softly.assertThat(actual.plannedMinutes())
                .as("Planned minutes")
                .isEqualTo(expected.plannedMinutes());

        softly.assertThat(actual.overtimeMinutes())
                .as("Overtime minutes")
                .isEqualTo(expected.overtimeMinutes());

        softly.assertThat(actual.forgivenessApplied())
                .as("Forgiveness applied")
                .isEqualTo(expected.forgivenessApplied());
        if (expected.calculationMessage() != null) {
            softly.assertThat(actual.forgivenessApplied())
                    .as("Forgiveness applied")
                    .isEqualTo(expected.forgivenessApplied());
        } else {
            softly.assertThat(actual.calculationMessage())
                    .as("Calculation message")
                    .isNotBlank();
        }

        softly.assertAll();
    }
```

## 4. Validation Steps

```bash
./gradlew :component-test:compileTestJava
```
