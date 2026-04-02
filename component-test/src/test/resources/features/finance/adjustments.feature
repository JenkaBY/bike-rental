@ReinitializeSystemLedgers
Feature: Manual balance adjustments
  As an administrator
  I want to apply manual balance corrections to a customer's wallet
  So that I can compensate for system errors or apply penalty deductions

  Background:
    Given the request header "Content-Type" is "application/vnd.bikerental.v1+json"
    And customers exist in the database with the following data
      | id   | phone       | firstName | lastName | email            | birthDate  |
      | CUS2 | +3706861555 | John      | Doe      | john@example.com | 1922-02-22 |
    And the following account records exist in db
      | id   | accountType | customerId |
      | ACC2 | CUSTOMER    | CUS2       |
    And the following sub-ledger records exist in db
      | id     | accountId | ledgerType      | balance | version | createdAt            | updatedAt            |
      | L_C_W2 | ACC2      | CUSTOMER_WALLET | 40.00   | 1       | 2026-03-27T00:00:00Z | 2026-03-27T00:00:00Z |
      | L_C_H2 | ACC2      | CUSTOMER_HOLD   | 30.00   | 1       | 2026-03-27T00:00:00Z | 2026-03-27T00:00:00Z |

  Scenario Outline: Successful top-up increases customer wallet balance
    Given the adjustment request is prepared with the following data
      | customerId | amount   | reason   | operatorId | idempotencyKey |
      | CUS2       | <amount> | <reason> | OP1        | IDK1           |
    When a POST request has been made to '/api/finance/adjustments' endpoint
    Then the response status is 201
    And the adjustment response contains a transactionId
    And the following sub-ledger records were persisted in db
      | id      | accountId | ledgerType      | balance         |
      | L_C_W2  | ACC2      | CUSTOMER_WALLET | <walletBalance> |
      | L_S_ADJ | ACC_S     | ADJUSTMENT      | <adjBalance>    |
    And the following transaction was persisted in db
      | customerId | type       | paymentMethod     | amount   | operatorId | reason   |
      | CUS2       | ADJUSTMENT | INTERNAL_TRANSFER | <amount> | OP1        | <reason> |
    Examples:
      | amount | walletBalance | adjBalance | reason                        |
      | 10.00  | 50.00         | -10.00     | Compensation for system error |

  Scenario Outline: Successful top-up increases customer wallet balance only once with same idempotency key
    Given the adjustment request is prepared with the following data
      | customerId | amount   | reason   | operatorId | idempotencyKey |
      | CUS2       | <amount> | <reason> | OP1        | IDK1           |
    When a POST request has been made to '/api/finance/adjustments' endpoint
    Then the response status is 201
    Given the adjustment request is prepared with the following data
      | customerId | amount   | reason   | operatorId | idempotencyKey |
      | CUS2       | <amount> | <reason> | OP1        | IDK1           |
    When a POST request has been made to '/api/finance/adjustments' endpoint
    Then the response status is 201
    And the adjustment response contains a transactionId
    And the following sub-ledger records were persisted in db
      | id      | accountId | ledgerType      | balance         |
      | L_C_W2  | ACC2      | CUSTOMER_WALLET | <walletBalance> |
      | L_S_ADJ | ACC_S     | ADJUSTMENT      | <adjBalance>    |
    And the following transaction was persisted in db
      | customerId | type       | paymentMethod     | amount   | operatorId | reason   |
      | CUS2       | ADJUSTMENT | INTERNAL_TRANSFER | <amount> | OP1        | <reason> |
    Examples:
      | amount | walletBalance | adjBalance | reason                        |
      | 10.00  | 50.00         | -10.00     | Compensation for system error |

  Scenario Outline: Successful deduction decreases customer wallet balance
    Given the adjustment request is prepared with the following data
      | customerId | amount   | reason            | operatorId | idempotencyKey |
      | CUS2       | <amount> | Penalty deduction | OP1        | IDK1           |
    When a POST request has been made to '/api/finance/adjustments' endpoint
    Then the response status is 201
    And the adjustment response contains a transactionId
    And the following sub-ledger records were persisted in db
      | id      | accountId | ledgerType      | balance     |
      | L_C_W2  | ACC2      | CUSTOMER_WALLET | <walletBal> |
      | L_S_ADJ | ACC_S     | ADJUSTMENT      | <adjBal>    |
    And the following transaction was persisted in db
      | customerId | type       | paymentMethod     | amount   | operatorId | reason            |
      | CUS2       | ADJUSTMENT | INTERNAL_TRANSFER | <absAmt> | OP1        | Penalty deduction |
    Examples:
      | amount | absAmt | walletBal | adjBal |
      | -15.00 | 15.00  | 25.00     | 15.00  |

  Scenario: Deduction rejected when wallet balance is insufficient
    Given the adjustment request is prepared with the following data
      | customerId | amount | reason            | operatorId | idempotencyKey |
      | CUS2       | -50.00 | Penalty deduction | OP1        | IDK1           |
    When a POST request has been made to '/api/finance/adjustments' endpoint
    Then the response status is 422
    And there are only 0 transactions in db
    And the following sub-ledger record was persisted in db
      | id     | accountId | ledgerType      | balance |
      | L_C_W2 | ACC2      | CUSTOMER_WALLET | 40.00   |
