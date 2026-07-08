<task_file_template>

# Task 010: Migrate Scenario A ("Activate a DRAFT rental...") to the signing flow

> **Applied Skill:** `spring-boot-java-cucumber` / `component-tests` — reuses the new `the rental is activated via
> signing` step (Task 009) in place of the two old `the lifecycle request is | ACTIVE | ...` / PATCH-lifecycles
> steps; drops the two `Then the rental response only contains ...` assertions that would otherwise read the
> `SignatureCreatedResponse` from the last HTTP call instead of a `RentalResponse` (see Task 009 notes), relying
> instead on the already-present `rental was persisted in database` / `rental equipments were persisted in database`
> DB-read assertions. The sub-ledger and `rental started event` assertions are unchanged (design.md §5: hold timing
> and event firing are unaffected by this migration). Depends on Tasks 008 and 009.

## 1. Objective

Replace the legacy direct `PATCH /lifecycles {ACTIVE}` activation in the Scenario Outline
"Activate a DRAFT rental — status becomes ACTIVE, hold placed, event published" with the new composite signing step,
and drop the two response-shape assertions that no longer apply after migration.

## 2. File to Modify / Create

* **File Path:** `component-test/src/test/resources/features/rental/rental-lifecycle.feature`
* **Action:** Modify Existing File

## 3. Code Implementation

* **Location:** The `Scenario Outline: Activate a DRAFT rental — status becomes ACTIVE, hold placed, event
  published` block (the first scenario after `Background:`, right after Task 008's fixture row was appended to
  `Background`).

Replace this exact block:

```gherkin
  Scenario Outline: Activate a DRAFT rental — status becomes ACTIVE, hold placed, event published
    Given now is "<now>"
    And a single rental exists in the database with the following data
      | id         | customerId | status | plannedDuration | createdAt | updatedAt |
      | <rentalId> | <customer> | DRAFT  | 120             | <now>     | <now>     |
    And rental equipments exist in the database with the following data
      | rentalId   | equipmentId   | equipmentUid   | equipmentType | tariffId   | status   | startedAt           | expectedReturnAt    | estimatedCost   | createdAt           | updatedAt           |
      | <rentalId> | <equipmentId> | <equipmentUid> | BICYCLE       | <tariffId> | ASSIGNED | 2026-04-28T09:00:00 | 2026-04-28T11:00:00 | <estimatedCost> | 2026-04-28T09:00:00 | 2026-04-28T09:00:00 |
    And the lifecycle request is
      | status | operatorId |
      | ACTIVE | OP1        |
    When a PATCH request has been made to "/api/rentals/{requestedObjectId}/lifecycles" endpoint with context
    Then the response status is 200
    And the rental response only contains
      | customerId | status | estimatedCost   | plannedDuration   | startedAt |
      | <customer> | ACTIVE | <estimatedCost> | <plannedDuration> | <now>     |
    And the rental response only contains rental equipments
      | equipmentId   | equipmentUid   | status | tariffId   | estimatedCost   | finalCost |
      | <equipmentId> | <equipmentUid> | ACTIVE | <tariffId> | <estimatedCost> |           |
    #    rental module
    And rental was persisted in database
      | customerId | status | plannedDuration   |
      | <customer> | ACTIVE | <plannedDuration> |
    And rental equipments were persisted in database
      | rentalId   | equipmentId   | equipmentUid   | status | estimatedCost   | tariffId   |
      | <rentalId> | <equipmentId> | <equipmentUid> | ACTIVE | <estimatedCost> | <tariffId> |
    #    finance module
    And the following sub-ledger records were persisted in db
      | id     | accountId | ledgerType      | balance |
      | L_C_W1 | ACC1      | CUSTOMER_WALLET | 284.00  |
      | L_C_H1 | ACC1      | CUSTOMER_HOLD   | 16.00   |
    #    events
    And the following rental started event was published
      | customerId |
      | <customer> |
    Examples:
      | rentalId | equipmentUid | equipmentId | tariffId | customer | now                 | plannedDuration | estimatedCost |
      | RENT1    | BIKE-001     | 1           | 10       | CUS1     | 2026-05-01T10:00:00 | 120             | 16.00         |
```

with:

```gherkin
  Scenario Outline: Activate a DRAFT rental — status becomes ACTIVE, hold placed, event published
    Given now is "<now>"
    And a single rental exists in the database with the following data
      | id         | customerId | status | plannedDuration | createdAt | updatedAt |
      | <rentalId> | <customer> | DRAFT  | 120             | <now>     | <now>     |
    And rental equipments exist in the database with the following data
      | rentalId   | equipmentId   | equipmentUid   | equipmentType | tariffId   | status   | startedAt           | expectedReturnAt    | estimatedCost   | createdAt           | updatedAt           |
      | <rentalId> | <equipmentId> | <equipmentUid> | BICYCLE       | <tariffId> | ASSIGNED | 2026-04-28T09:00:00 | 2026-04-28T11:00:00 | <estimatedCost> | 2026-04-28T09:00:00 | 2026-04-28T09:00:00 |
    And the rental is activated via signing
    #    rental module
    And rental was persisted in database
      | customerId | status | plannedDuration   |
      | <customer> | ACTIVE | <plannedDuration> |
    And rental equipments were persisted in database
      | rentalId   | equipmentId   | equipmentUid   | status | estimatedCost   | tariffId   |
      | <rentalId> | <equipmentId> | <equipmentUid> | ACTIVE | <estimatedCost> | <tariffId> |
    #    finance module
    And the following sub-ledger records were persisted in db
      | id     | accountId | ledgerType      | balance |
      | L_C_W1 | ACC1      | CUSTOMER_WALLET | 284.00  |
      | L_C_H1 | ACC1      | CUSTOMER_HOLD   | 16.00   |
    #    events
    And the following rental started event was published
      | customerId |
      | <customer> |
    Examples:
      | rentalId | equipmentUid | equipmentId | tariffId | customer | now                 | plannedDuration | estimatedCost |
      | RENT1    | BIKE-001     | 1           | 10       | CUS1     | 2026-05-01T10:00:00 | 120             | 16.00         |
```

Note: the `Then the response status is 200` line is intentionally dropped along with the two `rental response only
contains ...` lines, since the composite step's last HTTP call already asserts nothing about status by itself — the
subsequent DB-read `Then` steps are the source of truth for this scenario now. Do not add a manual status-200
assertion; none of the delegated `WebRequestSteps` calls fail the scenario on a non-matching status unless a `Then
the response status is` step explicitly checks it, and this scenario's later `rental was persisted...`/`rental
equipments were persisted...`/`rental started event...` steps only pass if signing actually succeeded (200/201), so
they are sufficient to prove the flow worked end-to-end.

## 4. Validation Steps

Execute the following command. Do NOT run the full application server. Assume the DB is already up.

```bash
./gradlew :component-test:test "-Dspring.profiles.active=test"
```

</task_file_template>
