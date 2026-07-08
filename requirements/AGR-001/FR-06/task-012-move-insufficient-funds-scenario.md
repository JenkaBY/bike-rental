<task_file_template>

# Task 012: Move the insufficient-wallet-balance scenario to rental-signing-lifecycle.feature

> **Applied Skill:** `spring-boot-java-cucumber` / `component-tests` — the insufficient-funds business rule now
> triggers at `DRAFT → AWAITING_SIGNATURE` (FR-02), not at activation (design.md §5), so this business-rule-failure
> scenario belongs in `rental-signing-lifecycle.feature` (happy paths + business-rule failures only, per
> `.claude/rules/component-tests.md`); it reuses the file's existing `the lifecycle request is | AWAITING_SIGNATURE |
> OP1 |` + PATCH steps (already used throughout that file) — no new step needed. Depends on Tasks 010/011 (same
> source file `rental-lifecycle.feature`, sequential edits to avoid merge conflicts).

## 1. Objective

Remove the "Attempt to activate a DRAFT rental with insufficient wallet balance" scenario from
`rental-lifecycle.feature` and re-add it, renamed and adapted to the prepare-signing transition, in
`rental-signing-lifecycle.feature`. The 422 status, `$.title`/`$.detail`/`$.errorCode` values, and the
`rental.insufficient_funds` error code are unchanged — only the triggering transition changes from `ACTIVE` to
`AWAITING_SIGNATURE`.

## 2. File to Modify / Create

* **File Path:** `component-test/src/test/resources/features/rental/rental-lifecycle.feature`
* **Action:** Modify Existing File (remove the scenario)

* **File Path:** `component-test/src/test/resources/features/rental/rental-signing-lifecycle.feature`
* **Action:** Modify Existing File (add the scenario)

## 3. Code Implementation

### Edit A — remove from rental-lifecycle.feature

* **Location:** The `Scenario: Attempt to activate a DRAFT rental with insufficient wallet balance` block, located
  directly after the `Examples:` table of the (already-migrated in Task 010) `Scenario Outline: Activate a DRAFT
  rental...`, and directly before `Scenario Outline: Cancel a <rentalStatus> rental without hold...`.

Delete this entire block (including the blank line immediately after it, keeping exactly one blank line before the
next `Scenario Outline:`):

```gherkin
  Scenario: Attempt to activate a DRAFT rental with insufficient wallet balance
    Given a single rental exists in the database with the following data
      | id | customerId | status | plannedDuration | createdAt           | updatedAt           |
      | 1  | CUS2       | DRAFT  | 120             | 2026-04-28T09:00:00 | 2026-04-28T09:00:00 |
    And rental equipments exist in the database with the following data
      | rentalId | equipmentId | equipmentUid | equipmentType | tariffId | status   | startedAt           | expectedReturnAt    | estimatedCost | createdAt           | updatedAt           |
      | 1        | 1           | BIKE-001     | BICYCLE       | 10       | ASSIGNED | 2026-04-28T09:00:00 | 2026-04-28T11:00:00 | 16.00         | 2026-04-28T09:00:00 | 2026-04-28T09:00:00 |
    And the lifecycle request is
      | status | operatorId |
      | ACTIVE | OP1        |
    When a PATCH request has been made to "/api/rentals/{requestedObjectId}/lifecycles" endpoint with context
    Then the response status is 422
    And the response contains
      | path        | value                                                                     |
      | $.title     | Insufficient funds                                                        |
      | $.detail    | Insufficient wallet balance. Available: 10.00, requested deduction: 16.00 |
      | $.errorCode | rental.insufficient_funds                                                 |

```

`CUS2`/`ACC2`/`L_C_W2`/`L_C_H2` fixtures in `rental-lifecycle.feature`'s `Background` become unused after this
removal — leave them in place (do not touch `Background`, other scenarios may still rely on the general fixture
set staying stable; removing unused Background rows is out of scope for this task).

### Edit B — add to rental-signing-lifecycle.feature

* **Location:** Add as a new scenario at the end of the file, immediately after the last scenario
  (`Scenario: Zero-cost rental holds nothing`), preserving a single trailing blank line consistent with the rest of
  the file.

Note: `rental-signing-lifecycle.feature`'s `Background` already defines `CUS2`/`ACC2`/`L_C_W2` with a wallet balance
of `300.00` (see file lines 12, 32, 37-38) — that balance is high enough to cover the `16.00` hold, so it cannot be
reused for this insufficient-funds scenario, and the existing `CUS2` fixture rows must NOT be changed (other
scenarios in this file depend on that `300.00` balance). Instead, add a NEW third customer (`CUS3`) with a `10.00`
wallet balance to this file's `Background`, appended after the last existing row of each respective table:

To the customers table, add:

```gherkin
      | CUS3 | +79997773333 | Jamie     | Lee      | jamie@example.com | 1990-05-05 | null     |
```

To the account records table, add:

```gherkin
      | ACC3 | CUSTOMER    | CUS3       |
```

To the sub-ledger records table, add:

```gherkin
      | L_C_W3 | ACC3      | CUSTOMER_WALLET | 10.00   | 1       | 2026-03-27T00:00:00Z | 2026-03-27T00:00:00Z |
      | L_C_H3 | ACC3      | CUSTOMER_HOLD   | 0.00    | 1       | 2026-03-27T00:00:00Z | 2026-03-27T00:00:00Z |
```

Then append this new scenario at the end of the file:

```gherkin

  Scenario: Prepare signing rejected for insufficient wallet balance
    Given a single rental exists in the database with the following data
      | id | customerId | status | plannedDuration | createdAt           | updatedAt           |
      | 1  | CUS3       | DRAFT  | 120             | 2026-04-28T09:00:00 | 2026-04-28T09:00:00 |
    And rental equipments exist in the database with the following data
      | rentalId | equipmentId | equipmentUid | equipmentType | tariffId | status   | startedAt           | expectedReturnAt    | estimatedCost | createdAt           | updatedAt           |
      | 1        | 1           | BIKE-001     | BICYCLE       | 10       | ASSIGNED | 2026-04-28T09:00:00 | 2026-04-28T11:00:00 | 16.00         | 2026-04-28T09:00:00 | 2026-04-28T09:00:00 |
    And the lifecycle request is
      | status             | operatorId |
      | AWAITING_SIGNATURE | OP1        |
    When a PATCH request has been made to "/api/rentals/{requestedObjectId}/lifecycles" endpoint with context
    Then the response status is 422
    And the response contains
      | path        | value                                                                     |
      | $.title     | Insufficient funds                                                        |
      | $.detail    | Insufficient wallet balance. Available: 10.00, requested deduction: 16.00 |
      | $.errorCode | rental.insufficient_funds                                                 |
```

Before finalizing, re-read the current end of `rental-signing-lifecycle.feature` (the `Zero-cost rental holds
nothing` scenario) with the Read tool to confirm exact trailing whitespace/blank-line count, and match that file's
existing formatting exactly (2-space Gherkin indentation, one blank line between scenarios).

## 4. Validation Steps

Execute the following command. Do NOT run the full application server. Assume the DB is already up.

```bash
./gradlew :component-test:test "-Dspring.profiles.active=test"
```

</task_file_template>
