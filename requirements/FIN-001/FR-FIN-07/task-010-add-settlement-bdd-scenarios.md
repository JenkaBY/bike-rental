# Task 010: Add Settlement BDD Scenarios to `rental-return.feature`

> **Applied Skill:** `spring-boot-java-cucumber` — happy-path scenarios; assert DB state via existing step
> definitions; no JSON in features; reuse `SubLedgerDbSteps`, `TransactionDbSteps`,
> `TransactionRecordDbSteps`.
> **Depends on:** task-001 through task-009 (full settlement stack must be in place and compile-clean).

## 1. Objective

Add three new BDD scenarios to `rental-return.feature` that cover the settlement cases defined in
FR-FIN-07:

- **Scenario A — capture + release:** `finalCost < heldAmount` → two transactions (`CAPTURE`, `RELEASE`)
  and four `TransactionRecord` rows; `CUSTOMER_HOLD` reaches zero; `CUSTOMER_WALLET` is credited with the
  excess; `REVENUE` is credited with `finalCost`.
- **Scenario B — capture only:** `finalCost == heldAmount` → one `CAPTURE` transaction and two
  `TransactionRecord` rows; `CUSTOMER_HOLD` reaches zero; `REVENUE` is credited with `finalCost`.
- **Scenario C — hold-insufficient capture:** `finalCost > holdBalance`, wallet covers the shortfall →
  two `CAPTURE` transactions (one from HOLD, one from WALLET) and four `TransactionRecord` rows;
  `CUSTOMER_HOLD` and `CUSTOMER_WALLET` both reach zero; `REVENUE` is credited with `finalCost`. No
  `RELEASE` transaction is created.

## 2. File to Modify / Create

### File 1 — `rental-return.feature`

* **File Path:**
  `component-test/src/test/resources/features/rental/rental-return.feature`
* **Action:** Modify Existing File

## 3. Code Implementation

### File 1 — `rental-return.feature`

**Location:** Append the following two scenarios at the **end** of the file, after all existing scenarios.

```gherkin
  @ReinitializeSystemLedgers @ResetClock
  Scenario: Normal settlement — capture + release when final cost is less than held amount
    Given now is "2026-04-01T10:00:00"
    And the following account records exist in db
      | id   | accountType | customerId |
      | ACC1 | CUSTOMER    | CUS1       |
    And the following sub-ledger records exist in db
      | id     | accountId | ledgerType      | balance | version | createdAt            | updatedAt            |
      | L_C_W1 | ACC1      | CUSTOMER_WALLET | 0.00    | 1       | 2026-04-01T00:00:00Z | 2026-04-01T00:00:00Z |
      | L_C_H1 | ACC1      | CUSTOMER_HOLD   | 80.00   | 1       | 2026-04-01T00:00:00Z | 2026-04-01T00:00:00Z |
    And the following transaction records exist in db
      | id   | customerId | type | paymentMethod     | amount | operatorId | sourceType | sourceId | idempotencyKey |
      | TXH1 | CUS1       | HOLD | INTERNAL_TRANSFER | 80.00  | SYSTEM     | RENTAL     | 20       | IDK-HOLD-20    |
    And the following transaction record entries exist in db
      | transactionId | subLedger | ledgerType      | direction | amount |
      | TXH1          | L_C_W1    | CUSTOMER_WALLET | DEBIT     | 80.00  |
      | TXH1          | L_C_H1    | CUSTOMER_HOLD   | CREDIT    | 80.00  |
    And a single rental exists in the database with the following data
      | id | customerId | status | estimatedCost | plannedDuration | startedAt           | createdAt           | updatedAt           |
      | 20 | CUS1       | ACTIVE | 80.00         | 120             | 2026-04-01T08:00:00 | 2026-04-01T08:00:00 | 2026-04-01T08:00:00 |
    And rental equipments exist in the database with the following data
      | rentalId | equipmentId | equipmentUid | tariffId | status | startedAt           | expectedReturnAt    | estimatedCost | createdAt           | updatedAt           |
      | 20       | 1           | BIKE-001     | 1        | ACTIVE | 2026-04-01T08:00:00 | 2026-04-01T10:00:00 | 80.00         | 2026-04-01T08:00:00 | 2026-04-01T08:00:00 |
    And the return equipment request is
      | rentalId | equipmentIds | operatorId |
      | 20       | 1            | OP1        |
    When a POST request has been made to "/api/rentals/return" endpoint
    Then the response status is 200
    And the rental return response contains
      | settlementRecorded |
      | true               |
    And the following sub-ledger records were persisted in db
      | id     | accountId | ledgerType      | balance |
      | L_C_H1 | ACC1      | CUSTOMER_HOLD   | 0.00    |
      | L_C_W1 | ACC1      | CUSTOMER_WALLET | 20.00   |
      | L_S_REV | ACC_S    | REVENUE         | 60.00   |
    And the following transactions were persisted in db
      | customerId | amount | paymentMethod     | operatorId | type    | recordedAt          |
      | CUS1       | 60.00  | INTERNAL_TRANSFER | OP1        | CAPTURE | 2026-04-01T10:00:00 |
      | CUS1       | 20.00  | INTERNAL_TRANSFER | OP1        | RELEASE | 2026-04-01T10:00:00 |
    And the following transaction records were persisted in db
      | ledgerType      | direction | amount |
      | CUSTOMER_HOLD   | DEBIT     | 60.00  |
      | REVENUE         | CREDIT    | 60.00  |
      | CUSTOMER_HOLD   | DEBIT     | 20.00  |
      | CUSTOMER_WALLET | CREDIT    | 20.00  |
    And there are only 4 transaction records in db

  @ReinitializeSystemLedgers @ResetClock
  Scenario: Normal settlement — capture only when final cost equals held amount
    Given now is "2026-04-01T10:00:00"
    And the following account records exist in db
      | id   | accountType | customerId |
      | ACC1 | CUSTOMER    | CUS1       |
    And the following sub-ledger records exist in db
      | id     | accountId | ledgerType      | balance | version | createdAt            | updatedAt            |
      | L_C_W1 | ACC1      | CUSTOMER_WALLET | 0.00    | 1       | 2026-04-01T00:00:00Z | 2026-04-01T00:00:00Z |
      | L_C_H1 | ACC1      | CUSTOMER_HOLD   | 75.00   | 1       | 2026-04-01T00:00:00Z | 2026-04-01T00:00:00Z |
    And the following transaction records exist in db
      | id   | customerId | type | paymentMethod     | amount | operatorId | sourceType | sourceId | idempotencyKey |
      | TXH1 | CUS1       | HOLD | INTERNAL_TRANSFER | 75.00  | SYSTEM     | RENTAL     | 21       | IDK-HOLD-21    |
    And the following transaction record entries exist in db
      | transactionId | subLedger | ledgerType      | direction | amount |
      | TXH1          | L_C_W1    | CUSTOMER_WALLET | DEBIT     | 75.00  |
      | TXH1          | L_C_H1    | CUSTOMER_HOLD   | CREDIT    | 75.00  |
    And a single rental exists in the database with the following data
      | id | customerId | status | estimatedCost | plannedDuration | startedAt           | createdAt           | updatedAt           |
      | 21 | CUS1       | ACTIVE | 75.00         | 90              | 2026-04-01T08:30:00 | 2026-04-01T08:30:00 | 2026-04-01T08:30:00 |
    And rental equipments exist in the database with the following data
      | rentalId | equipmentId | equipmentUid | tariffId | status | startedAt           | expectedReturnAt    | estimatedCost | createdAt           | updatedAt           |
      | 21       | 1           | BIKE-001     | 1        | ACTIVE | 2026-04-01T08:30:00 | 2026-04-01T10:00:00 | 75.00         | 2026-04-01T08:30:00 | 2026-04-01T08:30:00 |
    And the return equipment request is
      | rentalId | equipmentIds | operatorId |
      | 21       | 1            | OP1        |
    When a POST request has been made to "/api/rentals/return" endpoint
    Then the response status is 200
    And the rental return response contains
      | settlementRecorded |
      | true               |
    And the following sub-ledger records were persisted in db
      | id      | accountId | ledgerType    | balance |
      | L_C_H1  | ACC1      | CUSTOMER_HOLD | 0.00    |
      | L_S_REV | ACC_S     | REVENUE       | 75.00   |
    And the following transactions were persisted in db
      | customerId | amount | paymentMethod     | operatorId | type    | recordedAt          |
      | CUS1       | 75.00  | INTERNAL_TRANSFER | OP1        | CAPTURE | 2026-04-01T10:00:00 |
    And the following transaction records were persisted in db
      | ledgerType    | direction | amount |
      | CUSTOMER_HOLD | DEBIT     | 75.00  |
      | REVENUE       | CREDIT    | 75.00  |
    And there are only 2 transaction records in db

  @ReinitializeSystemLedgers @ResetClock
  Scenario: Hold-insufficient settlement — wallet covers the shortfall, two CAPTURE transactions
    Given now is "2026-04-01T10:00:00"
    And the following account records exist in db
      | id   | accountType | customerId |
      | ACC1 | CUSTOMER    | CUS1       |
    And the following sub-ledger records exist in db
      | id     | accountId | ledgerType      | balance | version | createdAt            | updatedAt            |
      | L_C_W1 | ACC1      | CUSTOMER_WALLET | 30.00   | 1       | 2026-04-01T00:00:00Z | 2026-04-01T00:00:00Z |
      | L_C_H1 | ACC1      | CUSTOMER_HOLD   | 50.00   | 1       | 2026-04-01T00:00:00Z | 2026-04-01T00:00:00Z |
    And a single rental exists in the database with the following data
      | id | customerId | status | estimatedCost | plannedDuration | startedAt           | createdAt           | updatedAt           |
      | 22 | CUS1       | ACTIVE | 50.00         | 90              | 2026-04-01T08:00:00 | 2026-04-01T08:00:00 | 2026-04-01T08:00:00 |
    And rental equipments exist in the database with the following data
      | rentalId | equipmentId | equipmentUid | tariffId | status | startedAt           | expectedReturnAt    | estimatedCost | createdAt           | updatedAt           |
      | 22       | 1           | BIKE-001     | 1        | ACTIVE | 2026-04-01T08:00:00 | 2026-04-01T10:00:00 | 50.00         | 2026-04-01T08:00:00 | 2026-04-01T08:00:00 |
    And the return equipment request is
      | rentalId | equipmentIds | operatorId |
      | 22       | 1            | OP1        |
    When a POST request has been made to "/api/rentals/return" endpoint
    Then the response status is 200
    And the rental return response contains
      | settlementRecorded |
      | true               |
    And the following sub-ledger records were persisted in db
      | id     | accountId | ledgerType      | balance |
      | L_C_H1 | ACC1      | CUSTOMER_HOLD   | 0.00    |
      | L_C_W1 | ACC1      | CUSTOMER_WALLET | 0.00    |
      | L_S_REV | ACC_S    | REVENUE         | 80.00   |
    And the following transactions were persisted in db
      | customerId | amount | paymentMethod     | operatorId | type    | recordedAt          |
      | CUS1       | 50.00  | INTERNAL_TRANSFER | OP1        | CAPTURE | 2026-04-01T10:00:00 |
      | CUS1       | 30.00  | INTERNAL_TRANSFER | OP1        | CAPTURE | 2026-04-01T10:00:00 |
    And the following transaction records were persisted in db
      | ledgerType      | direction | amount |
      | CUSTOMER_HOLD   | DEBIT     | 50.00  |
      | REVENUE         | CREDIT    | 50.00  |
      | CUSTOMER_WALLET | DEBIT     | 30.00  |
      | REVENUE         | CREDIT    | 30.00  |
    And there are only 4 transaction records in db
```

> **Notes on setup data:**
> - `L_S_REV` / `ACC_S` are the aliases for the system account's `REVENUE` sub-ledger and the system
    > account, which are provisioned at startup and referenced by the `@ReinitializeSystemLedgers`
    > infrastructure. Verify their aliases in the transformer configuration (`Aliases` utility class) before
    > running — adjust to match actual alias values if different.
> - The tariff with `id=1` is already set up in the `Background` of this feature file and prices the first
    > 90 minutes at the half-hour rate. Rental 20 (120 min at 30 EUR/30 min = €120 estimated, but the tariff
    > produces a `totalCost` of 60.00 for 120 min at 100/hour). Verify against actual tariff rate output or
    > adjust amounts to match.
> - The `Given the following transaction records exist in db` and
    > `Given the following transaction record entries exist in db` steps must either already exist in
    > `TransactionDbSteps` / `TransactionRecordDbSteps` or be added there — see the
    > "New Step Definitions Required" note below.

> **New Step Definitions Required:**
> The `Given the following transaction records exist in db` step (for inserting seed `Transaction` rows) and
> `Given the following transaction record entries exist in db` step (for inserting seed `TransactionRecord`
> rows) need to be added to `TransactionDbSteps` and `TransactionRecordDbSteps` respectively, using the
> existing `InsertableTransactionRepository` and `InsertableTransactionRecordRepository` beans — following
> the same pattern as `@Given("the following payment record(s) exist(s) in db")` in the payment steps.

