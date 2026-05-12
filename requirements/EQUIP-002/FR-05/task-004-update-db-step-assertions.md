# Task 004: Add `conditionSlug` Assertion to `EquipmentDbSteps`

> **Applied Skill:** `spring-boot-java-cucumber/SKILL.md` — DB-layer step definition; AssertJ soft assertions for JPA
> entity verification.

## 1. Objective

Update `EquipmentDbSteps.assertEquipmentsPersisted` to also assert the `conditionSlug` field on the
persisted `EquipmentJpaEntity`, ensuring the DB-level verification covers the new column.

**Prerequisite:** FR-02 task-002 must be complete — `EquipmentJpaEntity` must have the
`conditionSlug` field (type `Condition`) before this assertion compiles.

## 2. File to Modify / Create

* **File Path:**
  `component-test/src/test/java/com/github/jenkaby/bikerental/componenttest/steps/equipment/EquipmentDbSteps.java`
* **Action:** Modify Existing File

## 3. Code Implementation

**Imports Required:**

```java
import com.github.jenkaby.bikerental.shared.domain.model.Condition;
```

Add after the existing imports block.

**Code to Add/Replace:**

* **Location:** Inside the `assertThat(sortedActual).zipSatisfy(...)` lambda in
  `assertEquipmentsPersisted`. Current last two lines before `softly.assertAll()`:

```java
                    softly.assertThat(actual.getCondition())
                            .as("Condition should match for serialNumberValue: %s", exp.getSerialNumber())
                            .isEqualTo(exp.getCondition());
                    softly.assertAll();
```

Replace with:

```java
                    softly.assertThat(actual.getCondition())
                            .as("Condition notes should match for serialNumberValue: %s", exp.getSerialNumber())
                            .isEqualTo(exp.getCondition());
                    softly.assertThat(actual.getConditionSlug())
                            .as("Condition slug should match for serialNumberValue: %s", exp.getSerialNumber())
                            .isEqualTo(exp.getConditionSlug());
                    softly.assertAll();
```

## 4. Validation Steps

No standalone validation — run the full component test suite as part of task-005 validation.
