# Task 004: Add Component Tests for Batch Equipment Fetch Endpoint

> **Applied Skill:** `spring-boot-java-cucumber` — happy-path-only scenarios; reuse of existing `Background`
> data-seeding steps; new assertion steps added to `EquipmentWebSteps` for flat-list (non-paginated) responses;
> feature file organized under `features/equipment/`.

## 1. Objective

Add a Cucumber feature file covering the four happy-path scenarios of `GET /api/equipments/batch` and extend
`EquipmentWebSteps` with two new assertion step methods: one that asserts a flat list of `EquipmentResponse`
objects and one that asserts an empty response. The existing `assertResult` private helper in `EquipmentWebSteps`
is reused as-is.

## 2. Files to Modify / Create

### File A — Extend step definitions

* **File Path:**
  `component-test/src/test/java/com/github/jenkaby/bikerental/componenttest/steps/equipment/EquipmentWebSteps.java`
* **Action:** Modify Existing File

### File B — Add batch scenarios to the existing feature file

* **File Path:** `component-test/src/test/resources/features/equipment/equipment.feature`
* **Action:** Modify Existing File — append four new scenarios at the end of the file

---

## 3. Code Implementation

### File A — `EquipmentWebSteps.java`

**Imports Required:**

None — all required types (`EquipmentResponse`, `List`, `ScenarioContext`, assertion statics) are already imported.

**Code to Add/Replace:**

* **Location:** Inside `EquipmentWebSteps`, immediately after the existing
  `theEquipmentResponseContainsListOf` method (the `@Then("the equipment response only contains list of")` method)
  and before the first `private` helper method.

* **Current code (context lines):**

```java
    @Then("the equipment response only contains list of")
    public void theEquipmentResponseContainsListOf(List<EquipmentResponse> expectedResponses) {
        var actual = scenarioContext.getResponseAsPage(EquipmentResponse.class).items().stream()
                .sorted(BY_SERIAL_NUMBER)
                .toList();

        assertResult(expectedResponses, actual);
    }

    private static void assertResult(List<EquipmentResponse> expectedResponses, List<EquipmentResponse> actual) {
```

* **Snippet (insert between the two blocks above):**

```java
    @Then("the batch equipment response contains")
    public void theBatchEquipmentResponseContains(List<EquipmentResponse> expectedResponses) {
        var actual = scenarioContext.getResponseAsList(EquipmentResponse.class).stream()
                .sorted(BY_SERIAL_NUMBER)
                .toList();

        assertResult(expectedResponses, actual);
    }

    @Then("the batch equipment response is empty")
    public void theBatchEquipmentResponseIsEmpty() {
        assertThat(scenarioContext.getResponseAsList(EquipmentResponse.class)).isEmpty();
    }

```

---

### File B — `equipment.feature`

The existing Background seeds 7 equipment records. The batch scenarios reference records by their
Background-defined integer IDs. Every expected-response DataTable includes the `commissionedAt` column to be
consistent with all other scenarios in the file — empty string for records without a date, explicit date string
for record 4 (`EQ-004`, `commissionedAt=2026-01-30`).

**Location:** Append these scenarios at the very end of the existing
`component-test/src/test/resources/features/equipment/equipment.feature` file (after the last `Scenario` block).

* **Snippet:**

```gherkin
  Scenario: Batch fetch returns all matching equipment when all IDs exist
    When a GET request has been made to "/api/equipments/batch" endpoint with query parameters
      | ids |
      | 1,2 |
    Then the response status is 200
    And the batch equipment response contains
      | serialNumber | uid        | status    | type    | model   | commissionedAt | conditionNotes | condition |
      | EQ-001       | BIKE-001   | AVAILABLE | BICYCLE | Model A |                | Good           | GOOD      |
      | EQ-002       | E-BIKE-001 | RENTED    | SCOOTER | Model B |                | Excellent      | GOOD      |

  Scenario: Batch fetch silently omits non-existent IDs
    When a GET request has been made to "/api/equipments/batch" endpoint with query parameters
      | ids      |
      | 1,99,100 |
    Then the response status is 200
    And the batch equipment response contains
      | serialNumber | uid      | status    | type    | model   | commissionedAt | conditionNotes | condition |
      | EQ-001       | BIKE-001 | AVAILABLE | BICYCLE | Model A |                | Good           | GOOD      |

  Scenario: Batch fetch returns empty list when no IDs match any record
    When a GET request has been made to "/api/equipments/batch" endpoint with query parameters
      | ids      |
      | 91,92,93 |
    Then the response status is 200
    And the batch equipment response is empty

  Scenario: Batch fetch de-duplicates repeated IDs
    When a GET request has been made to "/api/equipments/batch" endpoint with query parameters
      | ids   |
      | 4,4,4 |
    Then the response status is 200
    And the batch equipment response contains
      | serialNumber | uid      | status | type    | model   | commissionedAt | conditionNotes | condition |
      | EQ-004       | BIKE-002 | BROKEN | BICYCLE | Model C | 2026-01-30     | Fair           | BROKEN    |
```

---

## 4. Validation Steps

skip