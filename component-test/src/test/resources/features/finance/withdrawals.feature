Feature: Fund Withdrawal
  As a staff member
  I want to record a cash or cashless withdrawal for a customer
  So that the customer receives their available balance and the shop asset account is updated

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
      | L_C_W2 | ACC2      | CUSTOMER_WALLET | 80.00   | 1       | 2026-03-27T00:00:00Z | 2026-03-27T00:00:00Z |
      | L_C_H2 | ACC2      | CUSTOMER_HOLD   | 20.00   | 1       | 2026-03-27T00:00:00Z | 2026-03-27T00:00:00Z |

  @ResetClock
  Scenario Outline: Successful partial cash withdrawal reduces customer wallet and credits the payout sub-ledger
    Given now is "<now>"
    And the withdrawal request is prepared with the following data
      | idempotencyKey   | customerId   | amount   | payoutMethod   | operatorId   |
      | <idempotencyKey> | <customerId> | <amount> | <payoutMethod> | <operatorId> |
    When a POST request has been made to "/api/finance/withdrawals" endpoint
    Then the response status is 201
    And the withdrawal response contains a transactionId
    And the following sub-ledger records were persisted in db
      | id     | accountId | ledgerType      | version | balance          |
      | L_C_W2 | ACC2      | CUSTOMER_WALLET | 2       | <expectedWallet> |
      | L_C_H2 | ACC2      | CUSTOMER_HOLD   | 2       | 20.00            |
      | L_S_C  | ACC_S     | CASH            | 1       | <amount>         |
    And the following transactions were persisted in db
      | idempotencyKey   | customerId   | amount   | paymentMethod  | operatorId   | type       | recordedAt |
      | <idempotencyKey> | <customerId> | <amount> | <payoutMethod> | <operatorId> | WITHDRAWAL | <now>      |
    And the following transaction records were persisted in db
      | subLedger | ledgerType      | direction | amount   |
      | L_S_CASH  | CASH            | CREDIT    | <amount> |
      | L_C_W2    | CUSTOMER_WALLET | DEBIT     | <amount> |
    And there are only 2 transaction records in db
    Examples:
      | idempotencyKey | customerId | amount | payoutMethod | operatorId | now                 | expectedWallet |
      | IDK1           | CUS2       | 30.00  | CASH         | OP1        | 2026-03-28T10:00:00 | 50.00          |

  @ResetClock
  Scenario Outline: Duplicate withdrawal submission is idempotent
    Given now is "<now>"
    And the withdrawal request is prepared with the following data
      | idempotencyKey   | customerId   | amount   | payoutMethod | operatorId   |
      | <idempotencyKey> | <customerId> | <amount> | CASH         | <operatorId> |
    When a POST request has been made to "/api/finance/withdrawals" endpoint
    Then the response status is 201
    And the withdrawal response contains a transactionId
    Given the withdrawal request is prepared with the following data
      | idempotencyKey   | customerId   | amount   | payoutMethod | operatorId   |
      | <idempotencyKey> | <customerId> | <amount> | CASH         | <operatorId> |
    When a POST request has been made to "/api/finance/withdrawals" endpoint
    Then the response status is 201
    And the withdrawal response contains a transactionId
    And the following sub-ledger records were persisted in db
      | id     | accountId | ledgerType      | balance |
      | L_C_W2 | ACC2      | CUSTOMER_WALLET | 50.00   |
      | L_C_H2 | ACC2      | CUSTOMER_HOLD   | 20.00   |
    And there are only 1 transactions in db
    And there are only 2 transaction records in db
    Examples:
      | idempotencyKey | customerId | amount | operatorId | now                 |
      | IDK1           | CUS2       | 30.00  | OP1        | 2026-03-28T10:00:00 |

  Scenario Outline: Withdrawal rejected when amount exceeds available balance
    Given the withdrawal request is prepared with the following data
      | idempotencyKey   | customerId   | amount   | payoutMethod | operatorId   |
      | <idempotencyKey> | <customerId> | <amount> | CASH         | <operatorId> |
    When a POST request has been made to "/api/finance/withdrawals" endpoint
    Then the response status is 422
    Examples:
      | idempotencyKey | customerId | amount | operatorId |
      | IDK1           | CUS2       | 60.01  | OP1        |
