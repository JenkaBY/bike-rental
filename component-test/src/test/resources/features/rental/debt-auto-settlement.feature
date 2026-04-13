@ReinitializeSystemLedgers @ResetClock
Feature: Automatic DEBT rental settlement on customer deposit

  Background:
    Given the request header "Content-Type" is "application/vnd.bikerental.v1+json"
    And customers exist in the database with the following data
      | id   | phone        | firstName | lastName | email             | birthDate  | comments |
      | CUS1 | +79995551111 | Alex      | Johnson  | null              | null       | null     |
      | CUS2 | +3706861555  | John      | Doe      | john@example.com  | 1922-02-22 | null     |
      | CUS4 | +3706861552  | Jane1     | Doe2     | jane1@example.com | null       | null     |
    And the following account records exist in db
      | id   | accountType | customerId |
      | ACC2 | CUSTOMER    | CUS2       |
      | ACC4 | CUSTOMER    | CUS4       |
    And the following sub-ledger records exist in db
      | id     | accountId | ledgerType      | balance | version | createdAt            | updatedAt            |
      | L_C_W2 | ACC2      | CUSTOMER_WALLET | 80.00   | 1       | 2026-03-27T00:00:00Z | 2026-03-27T00:00:00Z |
      | L_C_H2 | ACC2      | CUSTOMER_HOLD   | 0.00    | 1       | 2026-03-27T00:00:00Z | 2026-03-27T00:00:00Z |
      | L_C_W4 | ACC4      | CUSTOMER_WALLET | 90.00   | 1       | 2026-03-27T00:00:00Z | 2026-03-27T00:00:00Z |
      | L_C_H4 | ACC4      | CUSTOMER_HOLD   | 5.00    | 1       | 2026-03-27T00:00:00Z | 2026-03-27T00:00:00Z |
    And a single rental exists in the database with the following data
      | id | customerId | status | estimatedCost | plannedDuration | startedAt           | createdAt           | updatedAt           |
      | 12 | CUS2       | DEBT   | 80.00         | 120             | 2026-03-01T08:00:00 | 2026-03-01T08:00:00 | 2026-03-01T08:00:00 |
    And rental equipments exist in the database with the following data
      | rentalId | equipmentId | equipmentUid | equipmentType | tariffId | status   | startedAt           | expectedReturnAt    | estimatedCost | totalCost | createdAt           | updatedAt           |
      | 12       | 1           | BIKE-001     | bicycle       | 1        | RETURNED | 2026-03-01T08:00:00 | 2026-03-01T08:00:00 | 100.00        | 100       | 2026-03-01T08:00:00 | 2026-03-01T08:00:00 |

  Scenario Outline: Deposit fully covers a single DEBT rental
    Given now is "<now>"
    And the deposit request is prepared with the following data
      | idempotencyKey | customerId | amount | paymentMethod | operatorId |
      | IDK1           | CUS2       | 30     | CASH          | OP1        |
    When a POST request has been made to "/api/finance/deposits" endpoint
    Then the response status is 201
    And rental was persisted in database
      | id         | customerId | status    |
      | <rentalId> | CUS2       | COMPLETED |
    And the following sub-ledger records were persisted in db
      | id      | accountId | ledgerType      | balance |
      | L_C_H2  | ACC2      | CUSTOMER_HOLD   | 0.00    |
      | L_C_W2  | ACC2      | CUSTOMER_WALLET | 10.00   |
      | L_S_REV | ACC_S     | REVENUE         | 100.00  |
    And the following transactions were persisted in db
      | customerId | amount | paymentMethod     | operatorId | type    | sourceId   | sourceType |
      | CUS2       | 30.00  | CASH              | OP1        | DEPOSIT |            |            |
      | CUS2       | 100.00 | INTERNAL_TRANSFER | OP1        | CAPTURE | <rentalId> | RENTAL     |
    And the following transaction records were persisted in db
      | subLedger | ledgerType      | direction | amount |
      | L_C_W2    | CUSTOMER_WALLET | CREDIT    | 30.00  |
      | L_S_CASH  | CASH            | DEBIT     | 30.00  |
      | L_C_W2    | CUSTOMER_WALLET | DEBIT     | 100.00 |
      | L_S_REV   | REVENUE         | CREDIT    | 100.00 |
    Examples:
      | rentalId | now                 |
      | 12       | 2026-03-28T10:00:00 |

  Scenario: Deposit covers oldest DEBT rental but not the next
    Given now is "2026-03-28T10:00:00"
    And rental exists in the database with the following data
      | id | customerId | status | estimatedCost | plannedDuration | finalCost | startedAt           | createdAt           | updatedAt           |
      | 13 | CUS2       | DEBT   | 80.00         | 120             | 280.00    | 2026-03-01T08:00:00 | 2026-03-01T09:00:00 | 2026-03-01T09:00:00 |
    And rental equipments exist in the database with the following data
      | rentalId | equipmentId | equipmentUid | equipmentType | tariffId | status   | startedAt           | expectedReturnAt    | estimatedCost | totalCost | createdAt           | updatedAt           |
      | 13       | 1           | BIKE-001     | bicycle       | 1        | RETURNED | 2026-03-01T08:00:00 | 2026-03-01T08:00:00 | 100.00        | 280       | 2026-03-01T08:00:00 | 2026-03-01T08:00:00 |
    And the deposit request is prepared with the following data
      | idempotencyKey | customerId | amount | paymentMethod | operatorId |
      | IDK1           | CUS2       | 80     | CASH          | OP1        |
    When a POST request has been made to "/api/finance/deposits" endpoint
    Then the response status is 201
    And rentals were persisted in database
      | id | customerId | status    |
      | 12 | CUS2       | COMPLETED |
      | 13 | CUS2       | DEBT      |
    And the following sub-ledger records were persisted in db
      | id      | accountId | ledgerType      | balance |
      | L_C_H2  | ACC2      | CUSTOMER_HOLD   | 0.00    |
      | L_C_W2  | ACC2      | CUSTOMER_WALLET | 60.00   |
      | L_S_REV | ACC_S     | REVENUE         | 100.00  |
    And the following transactions were persisted in db
      | customerId | amount | paymentMethod     | operatorId | type    | sourceId | sourceType |
      | CUS2       | 80.00  | CASH              | OP1        | DEPOSIT |          |            |
      | CUS2       | 100.00 | INTERNAL_TRANSFER | OP1        | CAPTURE | 12       | RENTAL     |
    And the following transaction records were persisted in db
      | subLedger | ledgerType      | direction | amount |
      | L_C_W2    | CUSTOMER_WALLET | CREDIT    | 80.00  |
      | L_S_CASH  | CASH            | DEBIT     | 80.00  |
      | L_C_W2    | CUSTOMER_WALLET | DEBIT     | 100.00 |
      | L_S_REV   | REVENUE         | CREDIT    | 100.00 |

  Scenario: Deposit covers all DEBT rentals
    Given now is "2026-03-28T10:00:00"
    And rental exists in the database with the following data
      | id | customerId | status | estimatedCost | plannedDuration | startedAt           | createdAt           | updatedAt           |
      | 13 | CUS2       | DEBT   | 80.00         | 120             | 2026-03-01T08:00:00 | 2026-03-01T09:00:00 | 2026-03-01T09:00:00 |
    And rental equipments exist in the database with the following data
      | rentalId | equipmentId | equipmentUid | equipmentType | tariffId | status   | startedAt           | expectedReturnAt    | estimatedCost | totalCost | createdAt           | updatedAt           |
      | 13       | 1           | BIKE-001     | bicycle       | 1        | RETURNED | 2026-03-01T08:00:00 | 2026-03-01T08:00:00 | 100.00        | 110       | 2026-03-01T08:00:00 | 2026-03-01T08:00:00 |
    And the deposit request is prepared with the following data
      | idempotencyKey | customerId | amount | paymentMethod | operatorId |
      | IDK1           | CUS2       | 130    | CASH          | OP1        |
    When a POST request has been made to "/api/finance/deposits" endpoint
    Then the response status is 201
    And rentals were persisted in database
      | id | customerId | status    |
      | 12 | CUS2       | COMPLETED |
      | 13 | CUS2       | COMPLETED |
    And the following sub-ledger records were persisted in db
      | id      | accountId | ledgerType      | balance |
      | L_C_H2  | ACC2      | CUSTOMER_HOLD   | 0.00    |
      | L_C_W2  | ACC2      | CUSTOMER_WALLET | 0.00    |
      | L_S_REV | ACC_S     | REVENUE         | 210.00  |
    And the following transactions were persisted in db
      | customerId | amount | paymentMethod     | operatorId | type    | sourceId | sourceType |
      | CUS2       | 130.00 | CASH              | OP1        | DEPOSIT |          |            |
      | CUS2       | 100.00 | INTERNAL_TRANSFER | OP1        | CAPTURE | 12       | RENTAL     |
      | CUS2       | 110.00 | INTERNAL_TRANSFER | OP1        | CAPTURE | 13       | RENTAL     |
    And the following transaction records were persisted in db
      | subLedger | ledgerType      | direction | amount |
      | L_C_W2    | CUSTOMER_WALLET | CREDIT    | 130.00 |
      | L_S_CASH  | CASH            | DEBIT     | 130.00 |
      | L_C_W2    | CUSTOMER_WALLET | DEBIT     | 100.00 |
      | L_S_REV   | REVENUE         | CREDIT    | 100.00 |
      | L_C_W2    | CUSTOMER_WALLET | DEBIT     | 110.00 |
      | L_S_REV   | REVENUE         | CREDIT    | 110.00 |

  Scenario: Deposit covers DEBT rental from HOLD and WALLET
    Given now is "2026-03-28T10:00:00"
    And rental exists in the database with the following data
      | id | customerId | status | estimatedCost | plannedDuration | startedAt           | createdAt           | updatedAt           |
      | 13 | CUS4       | DEBT   | 80.00         | 120             | 2026-03-01T08:00:00 | 2026-03-01T09:00:00 | 2026-03-01T09:00:00 |
    And rental equipments exist in the database with the following data
      | rentalId | equipmentId | equipmentUid | equipmentType | tariffId | status   | startedAt           | expectedReturnAt    | estimatedCost | totalCost | createdAt           | updatedAt           |
      | 13       | 1           | BIKE-001     | bicycle       | 1        | RETURNED | 2026-03-01T08:00:00 | 2026-03-01T08:00:00 | 100.00        | 100       | 2026-03-01T08:00:00 | 2026-03-01T08:00:00 |
    And the deposit request is prepared with the following data
      | idempotencyKey | customerId | amount | paymentMethod | operatorId |
      | IDK1           | CUS4       | 55     | CASH          | OP1        |
    When a POST request has been made to "/api/finance/deposits" endpoint
    Then the response status is 201
    And rentals were persisted in database
      | id | customerId | status    |
      | 12 | CUS2       | DEBT      |
      | 13 | CUS4       | COMPLETED |
    And the following sub-ledger records were persisted in db
      | id       | accountId | ledgerType      | balance |
      | L_C_H4   | ACC4      | CUSTOMER_HOLD   | 0.00    |
      | L_C_W4   | ACC4      | CUSTOMER_WALLET | 50.00   |
      | L_S_REV  | ACC_S     | REVENUE         | 100.00  |
      | L_S_CASH | ACC_S     | CASH            | 55.00   |
    And the following transactions were persisted in db
      | customerId | amount | paymentMethod     | operatorId | type    | sourceId | sourceType |
      | CUS4       | 55.00  | CASH              | OP1        | DEPOSIT |          |            |
      | CUS4       | 5.00   | INTERNAL_TRANSFER | OP1        | CAPTURE | 13       | RENTAL     |
      | CUS4       | 95.00  | INTERNAL_TRANSFER | OP1        | CAPTURE | 13       | RENTAL     |
    And the following transaction records were persisted in db
      | subLedger | ledgerType      | direction | amount |
      | L_C_W4    | CUSTOMER_WALLET | CREDIT    | 55.00  |
      | L_S_CASH  | CASH            | DEBIT     | 55.00  |
      | L_C_H4    | CUSTOMER_HOLD   | DEBIT     | 5.00   |
      | L_S_REV   | REVENUE         | CREDIT    | 5.00   |
      | L_C_W4    | CUSTOMER_WALLET | DEBIT     | 95.00  |
      | L_S_REV   | REVENUE         | CREDIT    | 95.00  |

  Scenario: Deposit insufficient for any DEBT rental
    Given the deposit request is prepared with the following data
      | idempotencyKey | customerId | amount | paymentMethod | operatorId |
      | IDK1           | CUS2       | 5      | CASH          | OP1        |
    When a POST request has been made to "/api/finance/deposits" endpoint
    Then the response status is 201
    And rentals were persisted in database
      | id | customerId | status |
      | 12 | CUS2       | DEBT   |
    And the following sub-ledger records were persisted in db
      | id       | accountId | ledgerType      | balance |
      | L_C_H2   | ACC2      | CUSTOMER_HOLD   | 0.00    |
      | L_C_W2   | ACC2      | CUSTOMER_WALLET | 85.00   |
      | L_S_CASH | ACC_S     | CASH            | 5.00    |
    And the following transaction records were persisted in db
      | subLedger | ledgerType      | direction | amount |
      | L_C_W2    | CUSTOMER_WALLET | CREDIT    | 5.00   |
      | L_S_CASH  | CASH            | DEBIT     | 5.00   |

  Scenario: No DEBT rentals — deposit completes normally without side effects
    Given the deposit request is prepared with the following data
      | idempotencyKey | customerId | amount | paymentMethod | operatorId |
      | IDK2           | CUS4       | 100.0  | CASH          | OP1        |
    When a POST request has been made to "/api/finance/deposits" endpoint
    Then the response status is 201
    And there is only 1 transactions in db

