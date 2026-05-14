# Task 011: Add Date Range Scenarios to rental-query.feature

> **Applied Skill:** `spring-boot-java-cucumber` — Cucumber BDD component tests, happy paths only;
> Gherkin best practices from skill file

## 1. Objective

Add four `Scenario Outline` blocks to `rental-query.feature` covering the happy-path date range
filter combinations from FR-01 acceptance criteria:

1. `from` only — returns rentals with `createdAt` on or after the given date.
2. `to` only — returns rentals with `createdAt` on or before the given date.
3. Both `from` and `to` — returns rentals within the inclusive range (same-day counts).
4. Date range combined with `status` filter.

All four scenarios reuse the existing Background setup (customers, equipment, equipment types,
tariffs) that is already defined in the file.

## 2. File to Modify / Create

* **File Path:** `component-test/src/test/resources/features/rental/rental-query.feature`
* **Action:** Modify Existing File

## 3. Code Implementation

**Code to Add/Replace:**

* **Location:** Append to the end of the file, after the last existing `Scenario Outline` block.

```gherkin
  Scenario Outline: Filter rentals by from date only
    Given now is "2026-02-20T12:00:00"
    And rentals exist in the database with the following data
      | id | customerId | status | startedAt           | expectedReturnAt    | createdAt   | updatedAt   |
      | 1  | CUS1       | ACTIVE | 2026-02-10T09:00:00 | 2026-02-10T11:00:00 | <early>     | <early>     |
      | 2  | CUS2       | ACTIVE | 2026-02-15T09:00:00 | 2026-02-15T11:00:00 | <boundary>  | <boundary>  |
      | 3  | CUS1       | ACTIVE | 2026-02-18T09:00:00 | 2026-02-18T11:00:00 | <later>     | <later>     |
    And rental equipment exists in the database with the following data
      | rentalId | equipmentId | equipmentUid | equipmentType | tariffId | status | startedAt           | expectedReturnAt    | estimatedCost | finalCost | createdAt   |
      | 1        | 1           | BIKE-001     | BICYCLE       | 1        | ACTIVE | 2026-02-10T09:00:00 | 2026-02-10T11:00:00 | 200.00        |           | <early>     |
      | 2        | 2           | E-BIKE-001   | SCOOTER       | 2        | ACTIVE | 2026-02-15T09:00:00 | 2026-02-15T11:00:00 | 200.00        |           | <boundary>  |
      | 3        | 3           | E-BIKE-002   | SCOOTER       | 2        | ACTIVE | 2026-02-18T09:00:00 | 2026-02-18T11:00:00 | 200.00        |           | <later>     |
    When a GET request has been made to "/api/rentals" endpoint with query parameters
      | from       |
      | 2026-02-15 |
    Then the response status is 200
    And the rental summary response only contains page of
      | id | customerId | equipmentIds | status | startedAt           | expectedReturnAt    | overdueMin |
      | 2  | CUS2       | 2            | ACTIVE | 2026-02-15T09:00:00 | 2026-02-15T11:00:00 | 0          |
      | 3  | CUS1       | 3            | ACTIVE | 2026-02-18T09:00:00 | 2026-02-18T11:00:00 | 0          |
    Examples:
      | early               | boundary            | later               |
      | 2026-02-10T00:00:00 | 2026-02-15T00:00:00 | 2026-02-18T00:00:00 |

  Scenario Outline: Filter rentals by to date only
    Given now is "2026-02-20T12:00:00"
    And rentals exist in the database with the following data
      | id | customerId | status | startedAt           | expectedReturnAt    | createdAt   | updatedAt   |
      | 1  | CUS1       | ACTIVE | 2026-02-10T09:00:00 | 2026-02-10T11:00:00 | <early>     | <early>     |
      | 2  | CUS2       | ACTIVE | 2026-02-15T09:00:00 | 2026-02-15T11:00:00 | <boundary>  | <boundary>  |
      | 3  | CUS1       | ACTIVE | 2026-02-18T09:00:00 | 2026-02-18T11:00:00 | <later>     | <later>     |
    And rental equipment exists in the database with the following data
      | rentalId | equipmentId | equipmentUid | equipmentType | tariffId | status | startedAt           | expectedReturnAt    | estimatedCost | finalCost | createdAt   |
      | 1        | 1           | BIKE-001     | BICYCLE       | 1        | ACTIVE | 2026-02-10T09:00:00 | 2026-02-10T11:00:00 | 200.00        |           | <early>     |
      | 2        | 2           | E-BIKE-001   | SCOOTER       | 2        | ACTIVE | 2026-02-15T09:00:00 | 2026-02-15T11:00:00 | 200.00        |           | <boundary>  |
      | 3        | 3           | E-BIKE-002   | SCOOTER       | 2        | ACTIVE | 2026-02-18T09:00:00 | 2026-02-18T11:00:00 | 200.00        |           | <later>     |
    When a GET request has been made to "/api/rentals" endpoint with query parameters
      | to         |
      | 2026-02-15 |
    Then the response status is 200
    And the rental summary response only contains page of
      | id | customerId | equipmentIds | status | startedAt           | expectedReturnAt    | overdueMin |
      | 1  | CUS1       | 1            | ACTIVE | 2026-02-10T09:00:00 | 2026-02-10T11:00:00 | 0          |
      | 2  | CUS2       | 2            | ACTIVE | 2026-02-15T09:00:00 | 2026-02-15T11:00:00 | 0          |
    Examples:
      | early               | boundary            | later               |
      | 2026-02-10T00:00:00 | 2026-02-15T00:00:00 | 2026-02-18T00:00:00 |

  Scenario Outline: Filter rentals by from and to date range (same-day inclusive)
    Given now is "2026-02-20T12:00:00"
    And rentals exist in the database with the following data
      | id | customerId | status | startedAt           | expectedReturnAt    | createdAt   | updatedAt   |
      | 1  | CUS1       | ACTIVE | 2026-02-10T09:00:00 | 2026-02-10T11:00:00 | <early>     | <early>     |
      | 2  | CUS2       | ACTIVE | 2026-02-15T09:00:00 | 2026-02-15T11:00:00 | <boundary>  | <boundary>  |
      | 3  | CUS1       | ACTIVE | 2026-02-18T09:00:00 | 2026-02-18T11:00:00 | <later>     | <later>     |
    And rental equipment exists in the database with the following data
      | rentalId | equipmentId | equipmentUid | equipmentType | tariffId | status | startedAt           | expectedReturnAt    | estimatedCost | finalCost | createdAt   |
      | 1        | 1           | BIKE-001     | BICYCLE       | 1        | ACTIVE | 2026-02-10T09:00:00 | 2026-02-10T11:00:00 | 200.00        |           | <early>     |
      | 2        | 2           | E-BIKE-001   | SCOOTER       | 2        | ACTIVE | 2026-02-15T09:00:00 | 2026-02-15T11:00:00 | 200.00        |           | <boundary>  |
      | 3        | 3           | E-BIKE-002   | SCOOTER       | 2        | ACTIVE | 2026-02-18T09:00:00 | 2026-02-18T11:00:00 | 200.00        |           | <later>     |
    When a GET request has been made to "/api/rentals" endpoint with query parameters
      | from       | to         |
      | 2026-02-15 | 2026-02-15 |
    Then the response status is 200
    And the rental summary response only contains page of
      | id | customerId | equipmentIds | status | startedAt           | expectedReturnAt    | overdueMin |
      | 2  | CUS2       | 2            | ACTIVE | 2026-02-15T09:00:00 | 2026-02-15T11:00:00 | 0          |
    Examples:
      | early               | boundary            | later               |
      | 2026-02-10T00:00:00 | 2026-02-15T00:00:00 | 2026-02-18T00:00:00 |

  Scenario Outline: Filter rentals by date range combined with status
    Given now is "2026-02-20T12:00:00"
    And rentals exist in the database with the following data
      | id | customerId | status    | startedAt           | expectedReturnAt    | createdAt   | updatedAt   |
      | 1  | CUS1       | ACTIVE    | 2026-02-15T09:00:00 | 2026-02-15T11:00:00 | <inRange>   | <inRange>   |
      | 2  | CUS2       | COMPLETED | 2026-02-15T09:00:00 | 2026-02-15T11:00:00 | <inRange>   | <inRange>   |
      | 3  | CUS1       | ACTIVE    | 2026-02-10T09:00:00 | 2026-02-10T11:00:00 | <outOfRange>| <outOfRange>|
    And rental equipment exists in the database with the following data
      | rentalId | equipmentId | equipmentUid | equipmentType | tariffId | status   | startedAt           | expectedReturnAt    | estimatedCost | finalCost | createdAt    |
      | 1        | 1           | BIKE-001     | BICYCLE       | 1        | ACTIVE   | 2026-02-15T09:00:00 | 2026-02-15T11:00:00 | 200.00        |           | <inRange>    |
      | 2        | 2           | E-BIKE-001   | SCOOTER       | 2        | RETURNED | 2026-02-15T09:00:00 | 2026-02-15T11:00:00 | 200.00        | 200.00    | <inRange>    |
      | 3        | 3           | E-BIKE-002   | SCOOTER       | 2        | ACTIVE   | 2026-02-10T09:00:00 | 2026-02-10T11:00:00 | 200.00        |           | <outOfRange> |
    When a GET request has been made to "/api/rentals" endpoint with query parameters
      | status | from       | to         |
      | ACTIVE | 2026-02-15 | 2026-02-20 |
    Then the response status is 200
    And the rental summary response only contains page of
      | id | customerId | equipmentIds | status | startedAt           | expectedReturnAt    | overdueMin |
      | 1  | CUS1       | 1            | ACTIVE | 2026-02-15T09:00:00 | 2026-02-15T11:00:00 | 0          |
    Examples:
      | inRange             | outOfRange          |
      | 2026-02-15T00:00:00 | 2026-02-10T00:00:00 |
```

## 4. Validation Steps

skip
