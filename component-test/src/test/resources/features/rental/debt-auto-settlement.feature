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
      | ACC1 | CUSTOMER    | CUS1       |
      | ACC2 | CUSTOMER    | CUS2       |
      | ACC4 | CUSTOMER    | CUS4       |
    And the following sub-ledger records exist in db
      | id     | accountId | ledgerType      | balance | version | createdAt            | updatedAt            |
      | L_C_W2 | ACC2      | CUSTOMER_WALLET | 10.00   | 1       | 2026-03-27T00:00:00Z | 2026-03-27T00:00:00Z |
      | L_C_H2 | ACC2      | CUSTOMER_HOLD   | 150.00  | 1       | 2026-03-27T00:00:00Z | 2026-03-27T00:00:00Z |
      | L_C_W4 | ACC4      | CUSTOMER_WALLET | 90.00   | 1       | 2026-03-27T00:00:00Z | 2026-03-27T00:00:00Z |
      | L_C_H4 | ACC4      | CUSTOMER_HOLD   | 5.00    | 1       | 2026-03-27T00:00:00Z | 2026-03-27T00:00:00Z |
    And a single rental exists in the database with the following data
      | id    | customerId | status | estimatedCost | plannedDuration | startedAt           | createdAt           | updatedAt           |
      | RENT1 | CUS2       | DEBT   | 80.00         | 120             | 2026-03-01T08:00:00 | 2026-03-01T08:00:00 | 2026-03-01T08:00:00 |
    And rental equipments exist in the database with the following data
      | rentalId | equipmentId | equipmentUid | equipmentType | tariffId | status   | startedAt           | expectedReturnAt    | estimatedCost | finalCost | createdAt           | updatedAt           |
      | RENT1    | 1           | BIKE-001     | BICYCLE       | 1        | RETURNED | 2026-03-01T08:00:00 | 2026-03-01T08:00:00 | 80.00         | 100       | 2026-03-01T08:00:00 | 2026-03-01T08:00:00 |
    And the following transaction records exist in db
      | id  | type | paymentMethod | amount | customerId | operatorId | sourceType | sourceId | recordedAt          | idempotencyKey |
      | TX2 | HOLD | CASH          | 80.00  | CUS2       | OP1        | RENTAL     | RENT1    | 2026-03-21T10:00:00 | IDK4           |

  Scenario Outline: Deposit fully covers a single DEBT rental
    Given now is "<now>"
    And the deposit request is prepared with the following data
      | idempotencyKey | customerId | amount | paymentMethod | operatorId |
      | IDK1           | CUS2       | 20     | CARD_TERMINAL | OP1        |
    When a POST request has been made to "/api/finance/deposits" endpoint
    Then the response status is 201
    And rental was persisted in database
      | id         | customerId | status    |
      | <rentalId> | CUS2       | COMPLETED |
    And the following sub-ledger records were persisted in db
      | id      | accountId | ledgerType      | balance |
# it still holds another hold
      | L_C_H2  | ACC2      | CUSTOMER_HOLD   | 70.00   |
      | L_C_W2  | ACC2      | CUSTOMER_WALLET | 10.00   |
      | L_S_REV | ACC_S     | REVENUE         | 100.00  |
    And the following transactions were persisted in db
      | customerId | amount | paymentMethod     | operatorId | type    | sourceId   | sourceType |
#      it's really on the table but the transaction_records entries are missing
#      | CUS2       | 80.00  | INTERNAL_TRANSFER | OP1        | HOLD    | <rentalId> | RENTAL     |
      | CUS2       | 20.00  | CARD_TERMINAL     | OP1        | DEPOSIT |            |            |
      | CUS2       | 20.00  | INTERNAL_TRANSFER | OP1        | CAPTURE | <rentalId> | RENTAL     |
      | CUS2       | 80.00  | INTERNAL_TRANSFER | OP1        | CAPTURE | <rentalId> | RENTAL     |
    And the following transaction records were persisted in db
      | subLedger | ledgerType      | direction | amount |
      | L_S_CARD  | CARD_TERMINAL   | DEBIT     | 20.00  |
      | L_C_W2    | CUSTOMER_WALLET | CREDIT    | 20.00  |
      | L_C_H2    | CUSTOMER_HOLD   | DEBIT     | 80.00  |
      | L_S_REV   | REVENUE         | CREDIT    | 80.00  |
      | L_C_W2    | CUSTOMER_WALLET | DEBIT     | 20.00  |
      | L_S_REV   | REVENUE         | CREDIT    | 20.00  |
    Examples:
      | rentalId | now                 |
      | RENT1    | 2026-03-28T10:00:00 |

  Scenario: Deposit covers oldest DEBT rental but not the next
    Given now is "2026-03-28T10:00:00"
    And rental exists in the database with the following data
      | id    | customerId | status | estimatedCost | plannedDuration | finalCost | startedAt           | createdAt           | updatedAt           |
      | RENT2 | CUS2       | DEBT   | 70.00         | 120             | 280.00    | 2026-03-01T08:00:00 | 2026-03-01T09:00:00 | 2026-03-01T09:00:00 |
    And rental equipments exist in the database with the following data
      | rentalId | equipmentId | equipmentUid | equipmentType | tariffId | status   | startedAt           | expectedReturnAt    | estimatedCost | finalCost | createdAt           | updatedAt           |
      | RENT2    | 1           | BIKE-001     | BICYCLE       | 1        | RETURNED | 2026-03-01T08:00:00 | 2026-03-01T08:00:00 | 100.00        | 280       | 2026-03-01T08:00:00 | 2026-03-01T08:00:00 |
    And the following transaction records exist in db
      | id  | type | paymentMethod | amount | customerId | operatorId | sourceType | sourceId | recordedAt          | idempotencyKey |
      | TX3 | HOLD | CASH          | 70.00  | CUS2       | OP1        | RENTAL     | RENT2    | 2026-03-21T11:00:00 | IDK3           |
    And the deposit request is prepared with the following data
      | idempotencyKey | customerId | amount | paymentMethod | operatorId |
      | IDK1           | CUS2       | 50     | CASH          | OP1        |
    When a POST request has been made to "/api/finance/deposits" endpoint
    Then the response status is 201
    And rentals were persisted in database
      | id    | customerId | status    |
      | RENT1 | CUS2       | COMPLETED |
      | RENT2 | CUS2       | DEBT      |
    And the following sub-ledger records were persisted in db
      | id      | accountId | ledgerType      | balance |
#      the second rental hold (70)
      | L_C_H2  | ACC2      | CUSTOMER_HOLD   | 70.00   |
#      10 (initial) + 50 (new deposit) - 20 (debt actual = 100 final cost - 80 on hold)
      | L_C_W2  | ACC2      | CUSTOMER_WALLET | 40.00   |
#      the dept of the 2nd rental
      | L_S_REV | ACC_S     | REVENUE         | 100.00  |
    And the following transactions were persisted in db
      | customerId | amount | paymentMethod     | operatorId | type    | sourceId | sourceType |
      | CUS2       | 50.00  | CASH              | OP1        | DEPOSIT |          |            |
      | CUS2       | 80.00  | INTERNAL_TRANSFER | OP1        | CAPTURE | RENT1    | RENTAL     |
      | CUS2       | 20.00  | INTERNAL_TRANSFER | OP1        | CAPTURE | RENT1    | RENTAL     |
    And the following transaction records were persisted in db
      | subLedger | ledgerType      | direction | amount |
      | L_C_W2    | CUSTOMER_WALLET | CREDIT    | 50.00  |
      | L_S_CASH  | CASH            | DEBIT     | 50.00  |
      | L_C_H2    | CUSTOMER_HOLD   | DEBIT     | 80.00  |
      | L_S_REV   | REVENUE         | CREDIT    | 80.00  |
      | L_C_W2    | CUSTOMER_WALLET | DEBIT     | 20.00  |
      | L_S_REV   | REVENUE         | CREDIT    | 20.00  |

  Scenario: Deposit covers all DEBT rentals
    Given now is "2026-03-28T10:00:00"
    And rental exists in the database with the following data
      | id    | customerId | status | estimatedCost | plannedDuration | finalCost | startedAt           | createdAt           | updatedAt           |
      | RENT2 | CUS2       | DEBT   | 70.00         | 120             | 280.00    | 2026-03-01T08:00:00 | 2026-03-01T09:00:00 | 2026-03-01T09:00:00 |
    And rental equipments exist in the database with the following data
      | rentalId | equipmentId | equipmentUid | equipmentType | tariffId | status   | startedAt           | expectedReturnAt    | estimatedCost | finalCost | createdAt           | updatedAt           |
      | RENT2    | 1           | BIKE-001     | BICYCLE       | 1        | RETURNED | 2026-03-01T08:00:00 | 2026-03-01T08:00:00 | 100.00        | 280       | 2026-03-01T08:00:00 | 2026-03-01T08:00:00 |
    And the following transaction records exist in db
      | id  | type | paymentMethod | amount | customerId | operatorId | sourceType | sourceId | recordedAt          | idempotencyKey |
      | TX3 | HOLD | CASH          | 70.00  | CUS2       | OP1        | RENTAL     | RENT2    | 2026-03-21T11:00:00 | IDK3           |
    And the deposit request is prepared with the following data
      | idempotencyKey | customerId | amount | paymentMethod | operatorId |
      | IDK1           | CUS2       | 220    | CASH          | OP1        |
    When a POST request has been made to "/api/finance/deposits" endpoint
    Then the response status is 201
    And rentals were persisted in database
      | id    | customerId | status    |
      | RENT1 | CUS2       | COMPLETED |
      | RENT2 | CUS2       | COMPLETED |
    And the following sub-ledger records were persisted in db
      | id      | accountId | ledgerType      | balance |
#      the second rental hold (70)
      | L_C_H2  | ACC2      | CUSTOMER_HOLD   | 0.00    |
#      10 (initial) + 50 (new deposit) - 20 (debt actual = 100 final cost - 80 on hold)
      | L_C_W2  | ACC2      | CUSTOMER_WALLET | 0.00    |
#      the dept of the 2nd rental
      | L_S_REV | ACC_S     | REVENUE         | 380.00  |
    And the following transactions were persisted in db
      | customerId | amount | paymentMethod     | operatorId | type    | sourceId | sourceType |
      | CUS2       | 220.00 | CASH              | OP1        | DEPOSIT |          |            |
      | CUS2       | 80.00  | INTERNAL_TRANSFER | OP1        | CAPTURE | RENT1    | RENTAL     |
      | CUS2       | 20.00  | INTERNAL_TRANSFER | OP1        | CAPTURE | RENT1    | RENTAL     |
      | CUS2       | 70.00  | INTERNAL_TRANSFER | OP1        | CAPTURE | RENT2    | RENTAL     |
      | CUS2       | 210.00 | INTERNAL_TRANSFER | OP1        | CAPTURE | RENT2    | RENTAL     |
    And the following transaction records were persisted in db
      | subLedger | ledgerType      | direction | amount |
      | L_C_W2    | CUSTOMER_WALLET | CREDIT    | 220.00 |
      | L_S_CASH  | CASH            | DEBIT     | 220.00 |
      | L_C_H2    | CUSTOMER_HOLD   | DEBIT     | 80.00  |
      | L_S_REV   | REVENUE         | CREDIT    | 80.00  |
      | L_C_W2    | CUSTOMER_WALLET | DEBIT     | 20.00  |
      | L_S_REV   | REVENUE         | CREDIT    | 20.00  |
      | L_C_H2    | CUSTOMER_HOLD   | DEBIT     | 70.00  |
      | L_S_REV   | REVENUE         | CREDIT    | 70.00  |
      | L_C_W2    | CUSTOMER_WALLET | DEBIT     | 210.00 |
      | L_S_REV   | REVENUE         | CREDIT    | 210.00 |

  Scenario: Deposit insufficient for any DEBT rental
    Given the deposit request is prepared with the following data
      | idempotencyKey | customerId | amount | paymentMethod | operatorId |
      | IDK1           | CUS2       | 5      | CASH          | OP1        |
    When a POST request has been made to "/api/finance/deposits" endpoint
    Then the response status is 201
    And rentals were persisted in database
      | id    | customerId | status |
      | RENT1 | CUS2       | DEBT   |
    And the following sub-ledger records were persisted in db
      | id       | accountId | ledgerType      | balance |
      | L_C_H2   | ACC2      | CUSTOMER_HOLD   | 150.00  |
      | L_C_W2   | ACC2      | CUSTOMER_WALLET | 15.00   |
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
    And there is only 2 transactions in db
