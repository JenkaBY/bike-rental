# Task 003: Create `rental-available-equipments.feature`

> **Applied Skill:** `d:\Projects\private\bikerent\.github\skills\spring-boot-java-cucumber\SKILL.md` — Feature
> file conventions: `Background:` for shared setup, descriptive scenario names, `Scenario Outline:` where
> data-driven; reuse existing step vocabulary exclusively; no JSON in steps; test happy paths and business
> validations; do NOT cover request-format validation (belongs in WebMVC tests).

## 1. Objective

Create the Cucumber feature file that covers all 6 acceptance scenarios from `fr.md` for the
`GET /api/rentals/available-equipments` endpoint. The feature seeds a shared Background of 7 equipment
rows (from `equipment.feature`) plus 2 rental-occupancy rows (equipment ids 2 and 7), then asserts
filtering, occupancy exclusion, pagination, and invalid-param behaviour.

## 2. File to Modify / Create

* **File Path:**
  `component-test/src/test/resources/features/rental/rental-available-equipments.feature`
* **Action:** Create New File

## 3. Code Implementation

**Imports Required:** N/A — Gherkin file.

**Code to Add/Replace:**

* **Location:** New file — entire file content below.

> **Dataset reference (from `fr.md`):**
>
> | id | uid        | serialNumber | model      | condition | occupied? |
> |----|------------|--------------|------------|-----------|-----------|
> | 1  | BIKE-001   | EQ-001       | Model A    | GOOD      | no        |
> | 2  | E-BIKE-001 | EQ-002       | Model B    | GOOD      | ACTIVE    |
> | 3  | BIKE-003   | EQ-005       | Model C    | GOOD      | no        |
> | 4  | BIKE-002   | EQ-004       | Model C    | BROKEN    | no        |
> | 5  | BIKE-00-   | EQ-0066      | Model 1    | GOOD      | no        |
> | 6  | BIKE-0066  | EQ-007       | Model 2    | GOOD      | no        |
> | 7  | BIKE-009   | EQ-009       | Model 0066 | GOOD      | ASSIGNED  |
>
> Effective available set (GOOD + not occupied): ids **1, 3, 5, 6**.

* **Snippet:**

```gherkin
Feature: Available equipment query
  As a rental staff member
  I want to query available equipment for a new rental
  So that I can present only genuinely available options before creating a rental

  Background:
    Given the request header "Content-Type" is "application/vnd.bikerental.v1+json"
    And customers exist in the database with the following data
      | id   | phone        | firstName | lastName | email | birthDate | comments |
      | CUS1 | +79995551111 | Alex      | Johnson  | null  | null      | null     |
    And the following equipment statues exist in the database
      | slug      | name      | description       | transitions      |
      | BROKEN    | Broken    | Not Ready to rent | AVAILABLE        |
      | AVAILABLE | Available | Ready to rent     | BROKEN,RENTED    |
      | RENTED    | Rented    | In use already    | AVAILABLE,BROKEN |
    And the following equipment types exist in the database
      | slug    | name    | description |
      | BICYCLE | Bicycle | Two-wheeled |
      | SCOOTER | Scooter | Electric    |
    And the following equipment records exist in db
      | id | serialNumber | uid        | status    | type    | model      | condition |
      | 1  | EQ-001       | BIKE-001   | AVAILABLE | BICYCLE | Model A    | GOOD      |
      | 2  | EQ-002       | E-BIKE-001 | RENTED    | SCOOTER | Model B    | GOOD      |
      | 3  | EQ-005       | BIKE-003   | AVAILABLE | BICYCLE | Model C    | GOOD      |
      | 4  | EQ-004       | BIKE-002   | AVAILABLE | BICYCLE | Model C    | BROKEN    |
      | 5  | EQ-0066      | BIKE-00-   | AVAILABLE | BICYCLE | Model 1    | GOOD      |
      | 6  | EQ-007       | BIKE-0066  | AVAILABLE | BICYCLE | Model 2    | GOOD      |
      | 7  | EQ-009       | BIKE-009   | RENTED    | BICYCLE | Model 0066 | GOOD      |
    And rentals exist in the database with the following data
      | id | customerId | status | startedAt           | expectedReturnAt    | createdAt           | updatedAt           |
      | 1  | CUS1       | ACTIVE | 2026-01-01T10:00:00 | 2026-01-01T12:00:00 | 2026-01-01T10:00:00 | 2026-01-01T10:00:00 |
      | 2  | CUS1       | DRAFT | 2026-01-01T10:00:00 | 2026-01-01T12:00:00 | 2026-01-01T10:00:00 | 2026-01-01T10:00:00 |
    And rental equipment exists in the database with the following data
      | rentalId | equipmentId | equipmentUid | equipmentType | status   | startedAt           | createdAt           |
      | 1        | 2           | E-BIKE-001   | SCOOTER       | ACTIVE   | 2026-01-01T10:00:00 | 2026-01-01T10:00:00 |
      | 2        | 7           | BIKE-009     | BICYCLE       | ASSIGNED |                     | 2026-01-01T10:00:00 |

  Scenario: Returns all available equipment when no filter is applied
    When a GET request has been made to "/api/rentals/available-equipments" endpoint
    Then the response status is 200
    And the available equipment response only contains page of
      | id | uid       | serialNumber | typeSlug | model   |
      | 1  | BIKE-001  | EQ-001       | BICYCLE  | Model A |
      | 3  | BIKE-003  | EQ-005       | BICYCLE  | Model C |
      | 5  | BIKE-00-  | EQ-0066      | BICYCLE  | Model 1 |
      | 6  | BIKE-0066 | EQ-007       | BICYCLE  | Model 2 |
    And the response contains
      | path               | value |
      | $.totalItems       | 4     |
      | $.pageRequest.page | 0     |
      | $.pageRequest.size | 20    |

  Scenario: Filters by query text and excludes occupied equipment from results
    When a GET request has been made to "/api/rentals/available-equipments" endpoint with query parameters
      | q    |
      | 0066 |
    Then the response status is 200
    And the available equipment response only contains page of
      | id | uid       | serialNumber | typeSlug | model   |
      | 5  | BIKE-00-  | EQ-0066      | BICYCLE  | Model 1 |
      | 6  | BIKE-0066 | EQ-007       | BICYCLE  | Model 2 |
    And the response contains
      | path         | value |
      | $.totalItems | 2     |

  Scenario: Returns empty result when all matched equipment is occupied
    When a GET request has been made to "/api/rentals/available-equipments" endpoint with query parameters
      | q      |
      | E-BIKE |
    Then the response status is 200
    And the response contains
      | path         | value |
      | $.totalItems | 0     |

  Scenario: BROKEN condition equipment is always excluded regardless of occupancy
    When a GET request has been made to "/api/rentals/available-equipments" endpoint with query parameters
      | q      |
      | EQ-004 |
    Then the response status is 200
    And the response contains
      | path         | value |
      | $.totalItems | 0     |

  Scenario: Pagination parameters are reflected in the response and limit the returned items
    When a GET request has been made to "/api/rentals/available-equipments" endpoint with query parameters
      | page | size |
      | 0    | 2    |
    Then the response status is 200
    And the response contains
      | path               | value |
      | $.totalItems       | 4     |
      | $.pageRequest.page | 0     |
      | $.pageRequest.size | 2     |
    And the response list at "$.items" has size 2

  Scenario: Returns 400 when size parameter is negative
    When a GET request has been made to "/api/rentals/available-equipments" endpoint with query parameters
      | size |
      | -1   |
    Then the response status is 400
    And the response contains
      | path        | value                |
      | $.errorCode | CONSTRAINT_VIOLATION |
    And the response contains a UUID at "$.correlationId"
```

## 4. Insertion-Point Notes

* The step `"the following equipment statues exist in the database"` — note the intentional typo
  `statues` (not `statuses`); this is the exact string registered in `EquipmentStatusDbSteps`.
* The `condition` column value **must be uppercase** (`GOOD`, `BROKEN`) to match
  `Condition.valueOf(entry.get("condition"))` in `EquipmentJpaEntityTransformer`.
* The `rental_equipments.tariff_id` column is nullable per the Liquibase DDL — omitting `tariffId`
  from the DataTable is intentional.
* The `startedAt` column is left empty (not `null`) for the ASSIGNED rental-equipment row —
  `DataTableHelper.toLocalDateTime` treats empty string as `null`.
* The two `rental` rows are inserted first to satisfy the `fk_rent_equip_rental_id` FK constraint
  before inserting `rental_equipment` rows.
* `DbSteps.TABLE_TO_TRUNCATE` already includes `rental_equipments` and `rentals` — no changes
  to the teardown hook are required.

## 5. Validation Steps

Execute after all FR-01 through FR-06 tasks are complete:

```bash
./gradlew :component-test:test "-Dspring.profiles.active=test" --tests "*.rental.rental-available-equipments*"
```

Or run via the Cucumber tag (tag the feature with `@run` temporarily, switch `RunComponentTests` filter):

```bash
./gradlew :component-test:test "-Dspring.profiles.active=test" --tests RunComponentTests
```

All 6 scenarios must be green. Zero skipped.
