# Task 003: Add `conditionSlug` Assertion to `EquipmentWebSteps`

> **Applied Skill:** `spring-boot-java-cucumber/SKILL.md` — Cucumber step definition update; AssertJ soft assertions for
> response field verification.

## 1. Objective

Update the two assertion methods in `EquipmentWebSteps` that compare `EquipmentResponse` objects to
also assert the `conditionSlug` field, so component tests validate the new field end-to-end.

## 2. File to Modify / Create

* **File Path:**
  `component-test/src/test/java/com/github/jenkaby/bikerental/componenttest/steps/equipment/EquipmentWebSteps.java`
* **Action:** Modify Existing File

## 3. Code Implementation

**Imports Required:** No new imports needed.

### Change 1 — `theEquipmentResponseContains` (single-object assertion)

* **Location:** Inside the `theEquipmentResponseContains(EquipmentResponse expectedResponse)` method.
  Current content of the `assertSoftly` block:

```java
        assertSoftly(softly -> {
            if (expectedResponse.id() != null) {
                softly.assertThat(actual.id()).as("Equipment ID").isEqualTo(expectedResponse.id());
            }
            softly.assertThat(actual.serialNumber()).as("Serial number").isEqualTo(expectedResponse.serialNumber());
            softly.assertThat(actual.uid()).as("UID").isEqualTo(expectedResponse.uid());
            softly.assertThat(actual.type()).as("Equipment type slug").isEqualTo(expectedResponse.type());
            softly.assertThat(actual.model()).as("Model").isEqualTo(expectedResponse.model());
            softly.assertThat(actual.status()).as("Status slug").isEqualTo(expectedResponse.status());
            softly.assertThat(actual.commissionedAt()).as("Commissioned date").isEqualTo(expectedResponse.commissionedAt());
            softly.assertThat(actual.condition()).as("Condition").isEqualTo(expectedResponse.condition());
        });
```

Replace with:

```java
        assertSoftly(softly -> {
            if (expectedResponse.id() != null) {
                softly.assertThat(actual.id()).as("Equipment ID").isEqualTo(expectedResponse.id());
            }
            softly.assertThat(actual.serialNumber()).as("Serial number").isEqualTo(expectedResponse.serialNumber());
            softly.assertThat(actual.uid()).as("UID").isEqualTo(expectedResponse.uid());
            softly.assertThat(actual.type()).as("Equipment type slug").isEqualTo(expectedResponse.type());
            softly.assertThat(actual.model()).as("Model").isEqualTo(expectedResponse.model());
            softly.assertThat(actual.status()).as("Status slug").isEqualTo(expectedResponse.status());
            softly.assertThat(actual.commissionedAt()).as("Commissioned date").isEqualTo(expectedResponse.commissionedAt());
            softly.assertThat(actual.condition()).as("Condition notes").isEqualTo(expectedResponse.condition());
            softly.assertThat(actual.conditionSlug()).as("Condition slug").isEqualTo(expectedResponse.conditionSlug());
        });
```

### Change 2 — private `assertResult` (list assertion)

* **Location:** Inside the private `assertResult(List<EquipmentResponse>, List<EquipmentResponse>)` method.
  Current content of the inner `assertSoftly` block:

```java
            assertSoftly(softly -> {
                softly.assertThat(act.id()).isNotNull();
                softly.assertThat(act.serialNumber()).as("Serial number").isEqualTo(exp.serialNumber());
                softly.assertThat(act.uid()).as("UID").isEqualTo(exp.uid());
                softly.assertThat(act.type()).as("Equipment type slug").isEqualTo(exp.type());
                softly.assertThat(act.model()).as("Model").isEqualTo(exp.model());
                softly.assertThat(act.status()).as("Status slug").isEqualTo(exp.status());
                softly.assertThat(act.commissionedAt()).as("Commissioned date").isEqualTo(exp.commissionedAt());
                softly.assertThat(act.condition()).as("Condition").isEqualTo(exp.condition());
            });
```

Replace with:

```java
            assertSoftly(softly -> {
                softly.assertThat(act.id()).isNotNull();
                softly.assertThat(act.serialNumber()).as("Serial number").isEqualTo(exp.serialNumber());
                softly.assertThat(act.uid()).as("UID").isEqualTo(exp.uid());
                softly.assertThat(act.type()).as("Equipment type slug").isEqualTo(exp.type());
                softly.assertThat(act.model()).as("Model").isEqualTo(exp.model());
                softly.assertThat(act.status()).as("Status slug").isEqualTo(exp.status());
                softly.assertThat(act.commissionedAt()).as("Commissioned date").isEqualTo(exp.commissionedAt());
                softly.assertThat(act.condition()).as("Condition notes").isEqualTo(exp.condition());
                softly.assertThat(act.conditionSlug()).as("Condition slug").isEqualTo(exp.conditionSlug());
            });
```

## 4. Validation Steps

No standalone validation for this file — run component tests as part of task-005 validation.
