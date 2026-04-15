# Task 003: Delete V1 Component Tests and Step Definitions

> **Applied Skill:** `spring-boot-java-cucumber` — Cucumber feature files and step-definition classes
> that test V1 tariff endpoints (which return `404` after FR-TR-03) must be deleted so the
> component-test module compiles and no V1 scenario is executed.

## 1. Objective

Remove all Cucumber feature files, step-definition classes, and supporting repository helpers in the
`component-test` module that test V1 tariff endpoints (under `/api/tariffs`). After this task
`./gradlew :component-test:compileTestJava` must succeed.

## 2. Files to Delete

### Feature files

```
component-test/src/test/resources/features/tariff/tariff.feature
component-test/src/test/resources/features/tariff/tariff-selection.feature
```

### Step definition classes

```
component-test/src/test/java/com/github/jenkaby/bikerental/componenttest/steps/tariff/TariffWebSteps.java
component-test/src/test/java/com/github/jenkaby/bikerental/componenttest/steps/tariff/TariffDbSteps.java
```

Why deleted:

- `TariffWebSteps.java` imports `TariffRequest`, `TariffResponse`, `TariffSelectionResponse` (all
  deleted in task-004).
- `TariffDbSteps.java` imports `TariffJpaEntity` and `TariffJpaRepository` (both deleted in
  task-007).

### Component-test database repository helper

```
component-test/src/test/java/com/github/jenkaby/bikerental/componenttest/config/db/repository/InsertableTariffRepository.java
```

Why deleted: imports `TariffJpaEntity` (deleted in task-007).

> **Do NOT delete:**
> - `tariff-v2-management.feature`
> - `tariff-v2-rental-cost-calculation.feature`
> - `tariff-pricing-types.feature`
> - `TariffV2WebSteps.java`
> - `TariffV2DbSteps.java`
> - `RentalCostCalculationWebSteps.java`
>
> These are V2 features/step defs that must remain.

## 3. Code Implementation

No code is written; all actions are **file deletions**.

Delete the 5 files listed above:

```
component-test/src/test/resources/features/tariff/tariff.feature
component-test/src/test/resources/features/tariff/tariff-selection.feature
component-test/src/test/java/com/github/jenkaby/bikerental/componenttest/steps/tariff/TariffWebSteps.java
component-test/src/test/java/com/github/jenkaby/bikerental/componenttest/steps/tariff/TariffDbSteps.java
component-test/src/test/java/com/github/jenkaby/bikerental/componenttest/config/db/repository/InsertableTariffRepository.java
```

## 4. Validation Steps

```bash
./gradlew :component-test:compileTestJava "-Dspring.profiles.active=test"
```

Expected: BUILD SUCCESSFUL — `component-test` test sources compile with no unresolved references.
No Cucumber step-binding warning about unmatched steps should reference `TariffWebSteps` or
`TariffDbSteps`.
