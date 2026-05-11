# Task 005: Update `equipment.feature` Component Test with `conditionSlug`

> **Applied Skill:** `spring-boot-java-cucumber/SKILL.md` — Cucumber DataTable columns must match the target type's
> field names; `EquipmentResponse` record and `EquipmentJpaEntity` both gain `conditionSlug`.

## 1. Objective

Update `equipment.feature` to include `conditionSlug` in:

1. The **Background** DB-setup table — so the Cucumber `EquipmentJpaEntity` DataTable converter
   populates the new `condition_slug` column.
2. All **assertion tables** that use `"the equipment response only contains"` /
   `"the equipment response contains"` steps — so the new field is verified end-to-end.

**Note:** `EquipmentRequest` (the command DTO for create/update) is out of scope for FR-05. Do NOT
add `conditionSlug` to request-side tables.

## 2. File to Modify / Create

* **File Path:** `component-test/src/test/resources/features/equipment/equipment.feature`
* **Action:** Modify Existing File

## 3. Code Implementation

Apply each change below. For every table that needs updating, add `conditionSlug` as the last
column, right after the existing `condition` column.

---

### Change 1 — Background: DB-setup table

**Find:**

```gherkin
    And the following equipment records exist in db
      | id | serialNumber | uid        | status    | type    | model      | commissionedAt | condition |
      | 1  | EQ-001       | BIKE-001   | AVAILABLE | BICYCLE | Model A    |                | Good      |
      | 2  | EQ-002       | E-BIKE-001 | RENTED    | SCOOTER | Model B    |                | Excellent |
      | 3  | EQ-005       | BIKE-003   | AVAILABLE | BICYCLE | Model C    |                | Fair      |
      | 4  | EQ-004       | BIKE-002   | BROKEN    | BICYCLE | Model C    | 2026-01-30     | Fair      |
      | 5  | EQ-0066      | BIKE-00-   | AVAILABLE | BICYCLE | Model 1    |                | Good      |
      | 6  | EQ-007       | BIKE-0066  | AVAILABLE | BICYCLE | Model 2    |                | Good      |
      | 7  | EQ-009       | BIKE-009   | RENTED    | BICYCLE | Model 0066 |                | Good      |
```

**Replace with:**

```gherkin
    And the following equipment records exist in db
      | id | serialNumber | uid        | status    | type    | model      | commissionedAt | condition | conditionSlug |
      | 1  | EQ-001       | BIKE-001   | AVAILABLE | BICYCLE | Model A    |                | Good      | GOOD          |
      | 2  | EQ-002       | E-BIKE-001 | RENTED    | SCOOTER | Model B    |                | Excellent | GOOD          |
      | 3  | EQ-005       | BIKE-003   | AVAILABLE | BICYCLE | Model C    |                | Fair      | GOOD          |
      | 4  | EQ-004       | BIKE-002   | BROKEN    | BICYCLE | Model C    | 2026-01-30     | Fair      | MAINTENANCE   |
      | 5  | EQ-0066      | BIKE-00-   | AVAILABLE | BICYCLE | Model 1    |                | Good      | GOOD          |
      | 6  | EQ-007       | BIKE-0066  | AVAILABLE | BICYCLE | Model 2    |                | Good      | GOOD          |
      | 7  | EQ-009       | BIKE-009   | RENTED    | BICYCLE | Model 0066 |                | Good      | GOOD          |
```

---

### Change 2 — "Get equipment by ID" assertion table

**Find:**

```gherkin
    And the equipment response only contains
      | id   | serialNumber | uid      | status    | type    | model   | commissionedAt | condition |
      | <id> | EQ-001       | BIKE-001 | AVAILABLE | BICYCLE | Model A |                | Good      |
```

**Replace with:**

```gherkin
    And the equipment response only contains
      | id   | serialNumber | uid      | status    | type    | model   | commissionedAt | condition | conditionSlug |
      | <id> | EQ-001       | BIKE-001 | AVAILABLE | BICYCLE | Model A |                | Good      | GOOD          |
```

---

### Change 3 — "Search all equipment with default pagination" assertion table

**Find:**

```gherkin
    And the equipment response only contains list of
      | serialNumber | uid        | status    | type    | model      | commissionedAt | condition |
      | EQ-001       | BIKE-001   | AVAILABLE | BICYCLE | Model A    |                | Good      |
      | EQ-002       | E-BIKE-001 | RENTED    | SCOOTER | Model B    |                | Excellent |
      | EQ-005       | BIKE-003   | AVAILABLE | BICYCLE | Model C    |                | Fair      |
      | EQ-004       | BIKE-002   | BROKEN    | BICYCLE | Model C    | 2026-01-30     | Fair      |
      | EQ-0066      | BIKE-00-   | AVAILABLE | BICYCLE | Model 1    |                | Good      |
      | EQ-007       | BIKE-0066  | AVAILABLE | BICYCLE | Model 2    |                | Good      |
      | EQ-009       | BIKE-009   | RENTED    | BICYCLE | Model 0066 |                | Good      |
```

**Replace with:**

```gherkin
    And the equipment response only contains list of
      | serialNumber | uid        | status    | type    | model      | commissionedAt | condition | conditionSlug |
      | EQ-001       | BIKE-001   | AVAILABLE | BICYCLE | Model A    |                | Good      | GOOD          |
      | EQ-002       | E-BIKE-001 | RENTED    | SCOOTER | Model B    |                | Excellent | GOOD          |
      | EQ-005       | BIKE-003   | AVAILABLE | BICYCLE | Model C    |                | Fair      | GOOD          |
      | EQ-004       | BIKE-002   | BROKEN    | BICYCLE | Model C    | 2026-01-30     | Fair      | MAINTENANCE   |
      | EQ-0066      | BIKE-00-   | AVAILABLE | BICYCLE | Model 1    |                | Good      | GOOD          |
      | EQ-007       | BIKE-0066  | AVAILABLE | BICYCLE | Model 2    |                | Good      | GOOD          |
      | EQ-009       | BIKE-009   | RENTED    | BICYCLE | Model 0066 |                | Good      | GOOD          |
```

---

### Change 4 — "Search equipments by status and pagination" assertion table

**Find:**

```gherkin
    And the equipment response only contains list of
      | serialNumber | uid      | status    | type    | model   | commissionedAt | condition |
      | EQ-001       | BIKE-001 | AVAILABLE | BICYCLE | Model A |                | Good      |
      | EQ-005       | BIKE-003 | AVAILABLE | BICYCLE | Model C |                | Fair      |
```

**Replace with:**

```gherkin
    And the equipment response only contains list of
      | serialNumber | uid      | status    | type    | model   | commissionedAt | condition | conditionSlug |
      | EQ-001       | BIKE-001 | AVAILABLE | BICYCLE | Model A |                | Good      | GOOD          |
      | EQ-005       | BIKE-003 | AVAILABLE | BICYCLE | Model C |                | Fair      | GOOD          |
```

---

### Change 5 — "Search by search text" assertion table

**Find:**

```gherkin
    And the equipment response only contains list of
      | serialNumber | uid       | status    | type    | model      | commissionedAt | condition |
      | EQ-0066      | BIKE-00-  | AVAILABLE | BICYCLE | Model 1    |                | Good      |
      | EQ-007       | BIKE-0066 | AVAILABLE | BICYCLE | Model 2    |                | Good      |
      | EQ-009       | BIKE-009  | RENTED    | BICYCLE | Model 0066 |                | Good      |
```

**Replace with:**

```gherkin
    And the equipment response only contains list of
      | serialNumber | uid       | status    | type    | model      | commissionedAt | condition | conditionSlug |
      | EQ-0066      | BIKE-00-  | AVAILABLE | BICYCLE | Model 1    |                | Good      | GOOD          |
      | EQ-007       | BIKE-0066 | AVAILABLE | BICYCLE | Model 2    |                | Good      | GOOD          |
      | EQ-009       | BIKE-009  | RENTED    | BICYCLE | Model 0066 |                | Good      | GOOD          |
```

---

### Change 6 — "Search equipments by status and search text" assertion table

**Find:**

```gherkin
    And the equipment response only contains list of
      | serialNumber | uid       | status    | type    | model   | commissionedAt | condition |
      | EQ-0066      | BIKE-00-  | AVAILABLE | BICYCLE | Model 1 |                | Good      |
      | EQ-007       | BIKE-0066 | AVAILABLE | BICYCLE | Model 2 |                | Good      |
```

**Replace with:**

```gherkin
    And the equipment response only contains list of
      | serialNumber | uid       | status    | type    | model   | commissionedAt | condition | conditionSlug |
      | EQ-0066      | BIKE-00-  | AVAILABLE | BICYCLE | Model 1 |                | Good      | GOOD          |
      | EQ-007       | BIKE-0066 | AVAILABLE | BICYCLE | Model 2 |                | Good      | GOOD          |
```

---

### Change 7 — "Search equipments by type" assertion table

**Find:**

```gherkin
    And the equipment response only contains list of
      | serialNumber | uid        | status | type    | model   | commissionedAt | condition |
      | EQ-002       | E-BIKE-001 | RENTED | SCOOTER | Model B |                | Excellent |
```

**Replace with:**

```gherkin
    And the equipment response only contains list of
      | serialNumber | uid        | status | type    | model   | commissionedAt | condition | conditionSlug |
      | EQ-002       | E-BIKE-001 | RENTED | SCOOTER | Model B |                | Excellent | GOOD          |
```

---

### Change 8 — "Retrieve equipment by serial number" assertion table

**Find:**

```gherkin
    And the equipment response only contains
      | serialNumber | uid      | status    | type    | model   | commissionedAt | condition |
      | EQ-005       | BIKE-003 | AVAILABLE | BICYCLE | Model C |                | Fair      |
```

**Replace with:**

```gherkin
    And the equipment response only contains
      | serialNumber | uid      | status    | type    | model   | commissionedAt | condition | conditionSlug |
      | EQ-005       | BIKE-003 | AVAILABLE | BICYCLE | Model C |                | Fair      | GOOD          |
```

---

### Change 9 — "Retrieve equipment by uid" assertion table

**Find:**

```gherkin
    And the equipment response only contains
      | serialNumber | uid        | status | type    | model   | commissionedAt | condition |
      | EQ-002       | E-BIKE-001 | RENTED | SCOOTER | Model B |                | Excellent |
```

**Replace with:**

```gherkin
    And the equipment response only contains
      | serialNumber | uid        | status | type    | model   | commissionedAt | condition | conditionSlug |
      | EQ-002       | E-BIKE-001 | RENTED | SCOOTER | Model B |                | Excellent | GOOD          |
```

---

## 4. Validation Steps

Run the component test suite (requires DB running):

```bash
./gradlew :component-test:test "-Dspring.profiles.active=test"
```

All equipment scenarios must pass. A `conditionSlug` assertion failure indicates either:

- The DB migration (FR-01) has not been applied yet.
- `EquipmentJpaEntity` is missing the `conditionSlug` field (FR-02 task-002).
- The `EquipmentQueryMapper` mapping is incorrect (FR-05 task-002).
