# Task 007: Component Test — Deposit Triggers DEBT Rental Auto-Settlement

> **Applied Skill:** `spring-boot-java-cucumber` — Happy-path component tests only. Reuse existing step definitions from
`RentalDbSteps`, `TransactionDbSteps`, `SubLedgerDbSteps`, and `DepositWebSteps`. The event listener runs asynchronously
> post-commit; use `await()` for rental status assertions.

## 1. Objective

Write Cucumber component test scenarios covering FR-FIN-12's four BDD acceptance criteria: deposit settles one DEBT
rental, deposit settles oldest but not others, deposit insufficient for any, no DEBT rentals present.

## 2. File to Modify / Create

* **File Path:** `component-test/src/test/resources/features/rental/debt-auto-settlement.feature`
* **Action:** Create New File

## 3. Code Implementation

**Imports Required:** N/A (Gherkin feature file)

**Code to Add/Replace:**

* **Location:** New file — entire content below

```gherkin
Feature: Automatic DEBT rental settlement on customer deposit

  Background:
    Given now is "2026-03-01T10:00:00"

  Scenario Outline: Deposit fully covers a single DEBT rental
    Given a customer finance account exists in the database with the following data
      | id    | customerId | ledgerType      | balance |
      | ACC2  | CUS2       | CUSTOMER_WALLET | 0.00    |
      | ACC2  | CUS2       | CUSTOMER_HOLD   | 80.00   |
      | ACC_S | SYSTEM     | REVENUE         | 0.00    |
    And a single rental exists in the database with the following data
      | id         | customerId | status | estimatedCost | plannedDuration | finalCost | startedAt           | createdAt           | updatedAt           |
      | <rentalId> | CUS2       | DEBT   | 80.00         | 120             | 100.00    | 2026-03-01T08:00:00 | 2026-03-01T08:00:00 | 2026-03-01T08:00:00 |
    And the deposit request is
      | customerId | amount | paymentMethod | operatorId | idempotencyKey |
      | CUS2       | 30.00  | CASH          | OP1        | IK-001         |
    When a POST request has been made to "/api/finance/deposits" endpoint
    Then the response status is 200
    And rental(s) was/were persisted in database
      | id         | customerId | status    |
      | <rentalId> | CUS2       | COMPLETED |
    And the following sub-ledger records were persisted in db
      | id      | accountId | ledgerType      | balance |
      | L_C_H2  | ACC2      | CUSTOMER_HOLD   | 0.00    |
      | L_C_W2  | ACC2      | CUSTOMER_WALLET | 10.00   |
      | L_S_REV | ACC_S     | REVENUE         | 100.00  |
    And the following transactions were persisted in db
      | customerId | amount | paymentMethod     | operatorId | type    | sourceId   | sourceType |
      | CUS2       | 80.00  | INTERNAL_TRANSFER | OP1        | CAPTURE | <rentalId> | RENTAL     |
      | CUS2       | 20.00  | INTERNAL_TRANSFER | OP1        | CAPTURE | <rentalId> | RENTAL     |
    Examples:
      | rentalId |
      | 12       |

  Scenario Outline: Deposit covers oldest DEBT rental but not the next
    Given a customer finance account exists in the database with the following data
      | id    | customerId | ledgerType      | balance |
      | ACC2  | CUS2       | CUSTOMER_WALLET | 0.00    |
      | ACC2  | CUS2       | CUSTOMER_HOLD   | 160.00  |
      | ACC_S | SYSTEM     | REVENUE         | 0.00    |
    And rental(s) exist(s) in the database with the following data
      | id | customerId | status | estimatedCost | plannedDuration | finalCost | startedAt           | createdAt           | updatedAt           |
      | 12 | CUS2       | DEBT   | 80.00         | 120             | 100.00    | 2026-03-01T08:00:00 | 2026-03-01T08:00:00 | 2026-03-01T08:00:00 |
      | 13 | CUS2       | DEBT   | 80.00         | 120             | 280.00    | 2026-03-01T08:00:00 | 2026-03-01T09:00:00 | 2026-03-01T09:00:00 |
    And the deposit request is
      | customerId | amount | paymentMethod | operatorId | idempotencyKey |
      | CUS2       | 60.00  | CASH          | OP1        | IK-002         |
    When a POST request has been made to "/api/finance/deposits" endpoint
    Then the response status is 200
    And rental(s) was/were persisted in database
      | id | customerId | status    |
      | 12 | CUS2       | COMPLETED |
      | 13 | CUS2       | DEBT      |

  Scenario: Deposit insufficient for any DEBT rental
    Given a customer finance account exists in the database with the following data
      | id    | customerId | ledgerType      | balance |
      | ACC2  | CUS2       | CUSTOMER_WALLET | 0.00    |
      | ACC2  | CUS2       | CUSTOMER_HOLD   | 80.00   |
      | ACC_S | SYSTEM     | REVENUE         | 0.00    |
    And a single rental exists in the database with the following data
      | id | customerId | status | estimatedCost | plannedDuration | finalCost | startedAt           | createdAt           | updatedAt           |
      | 12 | CUS2       | DEBT   | 80.00         | 120             | 200.00    | 2026-03-01T08:00:00 | 2026-03-01T08:00:00 | 2026-03-01T08:00:00 |
    And the deposit request is
      | customerId | amount | paymentMethod | operatorId | idempotencyKey |
      | CUS2       | 5.00   | CASH          | OP1        | IK-003         |
    When a POST request has been made to "/api/finance/deposits" endpoint
    Then the response status is 200
    And rental(s) was/were persisted in database
      | id | customerId | status |
      | 12 | CUS2       | DEBT   |

  Scenario: No DEBT rentals — deposit completes normally without side effects
    Given a customer finance account exists in the database with the following data
      | id    | customerId | ledgerType      | balance |
      | ACC2  | CUS2       | CUSTOMER_WALLET | 0.00    |
      | ACC_S | SYSTEM     | REVENUE         | 0.00    |
    And the deposit request is
      | customerId | amount | paymentMethod | operatorId | idempotencyKey |
      | CUS2       | 50.00  | CASH          | OP1        | IK-004         |
    When a POST request has been made to "/api/finance/deposits" endpoint
    Then the response status is 200
    And there are/is only 1 transactions in db
```

## 4. Validation Steps

```bash
./gradlew :component-test:test "-Dspring.profiles.active=test"
```
