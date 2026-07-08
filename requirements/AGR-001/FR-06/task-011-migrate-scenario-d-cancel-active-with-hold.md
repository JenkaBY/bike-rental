<task_file_template>

# Task 011: Migrate Scenario D (both "Cancel an ACTIVE rental with hold..." scenarios) to the signing flow

> **Applied Skill:** `spring-boot-java-cucumber` / `component-tests` — same migration pattern as Task 010, applied to
> the activation half of two adjacent scenarios in the same file; the CANCELLED half of each scenario (second PATCH)
> is untouched since it is unaffected by this FR. Depends on Task 010 (same file — sequential edits to avoid merge
> conflicts) and Task 009 (the new step).

## 1. Objective

Replace the legacy direct `PATCH /lifecycles {ACTIVE}` activation step-pair with the new composite signing step in
both:

- "Cancel an ACTIVE rental with hold — hold released, equipment RETURNED, event published"
- "Cancel an ACTIVE rental with hold and 0 on wallet — hold released, equipment RETURNED, event published"

Each scenario's activation half currently ends with a `Then the response status is 200` +
`rental was persisted in database` + `rental equipments were persisted in database` +
`the following sub-ledger records were persisted in db` block right before the CANCELLED lifecycle-request block —
these DB-read/ledger assertions stay, only the `the lifecycle request is | ACTIVE | OP1` +
`When a PATCH request has been made to "/api/rentals/{requestedObjectId}/lifecycles" endpoint with context` +
`Then the response status is 200` triplet is replaced.

## 2. File to Modify / Create

* **File Path:** `component-test/src/test/resources/features/rental/rental-lifecycle.feature`
* **Action:** Modify Existing File

## 3. Code Implementation

### Edit A — "Cancel an ACTIVE rental with hold — hold released, equipment RETURNED, event published"

Replace this exact block:

```gherkin
    And the lifecycle request is
      | status | operatorId |
      | ACTIVE | OP1        |
    When a PATCH request has been made to "/api/rentals/{requestedObjectId}/lifecycles" endpoint with context
    Then the response status is 200
    #    rental module
    And rental was persisted in database
      | customerId | status |
      | CUS1       | ACTIVE |
    And rental equipments were persisted in database
      | equipmentId | equipmentUid | status |
      | 1           | BIKE-001     | ACTIVE |
    #    finance module
    And the following sub-ledger records were persisted in db
      | id     | accountId | ledgerType      | balance |
      | L_C_W1 | ACC1      | CUSTOMER_WALLET | 284.00  |
      | L_C_H1 | ACC1      | CUSTOMER_HOLD   | 16.00   |
    And the lifecycle request is
      | status    | operatorId |
      | CANCELLED | OP1        |
    When a PATCH request has been made to "/api/rentals/{requestedObjectId}/lifecycles" endpoint with context
    Then the response status is 200
    #    rental module
    And rental was persisted in database
      | customerId | status    | actualReturnAt      |
      | CUS1       | CANCELLED | 2026-05-01T10:00:00 |
    And rental equipments were persisted in database
      | equipmentId | equipmentUid | status   | actualReturnAt      |
      | 1           | BIKE-001     | RETURNED | 2026-05-01T10:00:00 |
    #    finance module — hold was placed, assert it has been released (balance 16.00 → 0.00)
    And the following sub-ledger records were persisted in db
      | id     | accountId | ledgerType      | balance |
      | L_C_W1 | ACC1      | CUSTOMER_WALLET | 300.00  |
      | L_C_H1 | ACC1      | CUSTOMER_HOLD   | 0.00    |
    #    events
    And the following rental cancelled event was published
      | rentalId | customerId | equipmentIds |
      | 1        | CUS1       | 1            |
```

with:

```gherkin
    And the rental is activated via signing
    #    rental module
    And rental was persisted in database
      | customerId | status |
      | CUS1       | ACTIVE |
    And rental equipments were persisted in database
      | equipmentId | equipmentUid | status |
      | 1           | BIKE-001     | ACTIVE |
    #    finance module
    And the following sub-ledger records were persisted in db
      | id     | accountId | ledgerType      | balance |
      | L_C_W1 | ACC1      | CUSTOMER_WALLET | 284.00  |
      | L_C_H1 | ACC1      | CUSTOMER_HOLD   | 16.00   |
    And the lifecycle request is
      | status    | operatorId |
      | CANCELLED | OP1        |
    When a PATCH request has been made to "/api/rentals/{requestedObjectId}/lifecycles" endpoint with context
    Then the response status is 200
    #    rental module
    And rental was persisted in database
      | customerId | status    | actualReturnAt      |
      | CUS1       | CANCELLED | 2026-05-01T10:00:00 |
    And rental equipments were persisted in database
      | equipmentId | equipmentUid | status   | actualReturnAt      |
      | 1           | BIKE-001     | RETURNED | 2026-05-01T10:00:00 |
    #    finance module — hold was placed, assert it has been released (balance 16.00 → 0.00)
    And the following sub-ledger records were persisted in db
      | id     | accountId | ledgerType      | balance |
      | L_C_W1 | ACC1      | CUSTOMER_WALLET | 300.00  |
      | L_C_H1 | ACC1      | CUSTOMER_HOLD   | 0.00    |
    #    events
    And the following rental cancelled event was published
      | rentalId | customerId | equipmentIds |
      | 1        | CUS1       | 1            |
```

### Edit B — "Cancel an ACTIVE rental with hold and 0 on wallet — hold released, equipment RETURNED, event published"

Replace this exact block:

```gherkin
    And the lifecycle request is
      | status | operatorId |
      | ACTIVE | OP1        |
    When a PATCH request has been made to "/api/rentals/{requestedObjectId}/lifecycles" endpoint with context
    Then the response status is 200
    #    rental module
    And rental was persisted in database
      | customerId | status |
      | CUS3       | ACTIVE |
    And rental equipments were persisted in database
      | equipmentId | equipmentUid | status |
      | 1           | BIKE-001     | ACTIVE |
    #    finance module
    And the following sub-ledger records were persisted in db
      | id     | accountId | ledgerType      | balance |
      | L_C_W3 | ACC3      | CUSTOMER_WALLET | 0.00    |
      | L_C_H3 | ACC3      | CUSTOMER_HOLD   | 16.00   |
    And the lifecycle request is
      | status    | operatorId |
      | CANCELLED | OP1        |
    When a PATCH request has been made to "/api/rentals/{requestedObjectId}/lifecycles" endpoint with context
    Then the response status is 200
    #    rental module
    And rental was persisted in database
      | customerId | status    | actualReturnAt      |
      | CUS3       | CANCELLED | 2026-05-01T10:00:00 |
    And rental equipments were persisted in database
      | equipmentId | equipmentUid | status   | actualReturnAt      |
      | 1           | BIKE-001     | RETURNED | 2026-05-01T10:00:00 |
    #    finance module — hold was placed, assert it has been released (balance 16.00 → 0.00)
    And the following sub-ledger records were persisted in db
      | id     | accountId | ledgerType      | balance |
      | L_C_W3 | ACC3      | CUSTOMER_WALLET | 16.00   |
      | L_C_H3 | ACC3      | CUSTOMER_HOLD   | 0.00    |
    #    events
    And the following rental cancelled event was published
      | rentalId | customerId | equipmentIds |
      | 1        | CUS3       | 1            |
```

with:

```gherkin
    And the rental is activated via signing
    #    rental module
    And rental was persisted in database
      | customerId | status |
      | CUS3       | ACTIVE |
    And rental equipments were persisted in database
      | equipmentId | equipmentUid | status |
      | 1           | BIKE-001     | ACTIVE |
    #    finance module
    And the following sub-ledger records were persisted in db
      | id     | accountId | ledgerType      | balance |
      | L_C_W3 | ACC3      | CUSTOMER_WALLET | 0.00    |
      | L_C_H3 | ACC3      | CUSTOMER_HOLD   | 16.00   |
    And the lifecycle request is
      | status    | operatorId |
      | CANCELLED | OP1        |
    When a PATCH request has been made to "/api/rentals/{requestedObjectId}/lifecycles" endpoint with context
    Then the response status is 200
    #    rental module
    And rental was persisted in database
      | customerId | status    | actualReturnAt      |
      | CUS3       | CANCELLED | 2026-05-01T10:00:00 |
    And rental equipments were persisted in database
      | equipmentId | equipmentUid | status   | actualReturnAt      |
      | 1           | BIKE-001     | RETURNED | 2026-05-01T10:00:00 |
    #    finance module — hold was placed, assert it has been released (balance 16.00 → 0.00)
    And the following sub-ledger records were persisted in db
      | id     | accountId | ledgerType      | balance |
      | L_C_W3 | ACC3      | CUSTOMER_WALLET | 16.00   |
      | L_C_H3 | ACC3      | CUSTOMER_HOLD   | 0.00    |
    #    events
    And the following rental cancelled event was published
      | rentalId | customerId | equipmentIds |
      | 1        | CUS3       | 1            |
```

Do NOT touch Scenario C ("Cancel a <rentalStatus> rental without hold — equipment RETURNED, hold balance unchanged")
between these two — it seeds status directly via DB fixture and never calls the activation endpoint.

## 4. Validation Steps

Execute the following command. Do NOT run the full application server. Assume the DB is already up.

```bash
./gradlew :component-test:test "-Dspring.profiles.active=test"
```

</task_file_template>
