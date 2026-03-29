Feature: Money movement between accounts
  As a staff member
  I want to record all money movements between accounts, including deposits, withdrawals, and transfers
  So that the financial records are accurate and up-to-date

  Background:
    Given the request header "Content-Type" is "application/vnd.bikerental.v1+json"
    And customers exist in the database with the following data
      | id   | phone       | firstName | lastName | email            | birthDate  |
      | CUS2 | +3706861555 | John      | Doe      | john@example.com | 1922-02-22 |
      | CUS3 | +3706861551 | Jane      | Doe      | jane@example.com | 1922-02-21 |
# System accounts and sub-ledgers are already in DB
    And the following account records exist in db
      | id   | accountType | customerId |
      | ACC2 | CUSTOMER    | CUS2       |
      | ACC3 | CUSTOMER    | CUS3       |
    And the following sub-ledger records exist in db
      | id     | accountId | ledgerType      | balance | version | createdAt            | updatedAt            |
      | L_C_W2 | ACC2      | CUSTOMER_WALLET | 0.00    | 1       | 2026-03-27T00:00:00Z | 2026-03-27T00:00:00Z |
      | L_C_H2 | ACC2      | CUSTOMER_HOLD   | 0.00    | 1       | 2026-03-27T00:00:00Z | 2026-03-27T00:00:00Z |
      | L_C_W3 | ACC3      | CUSTOMER_WALLET | 0.00    | 1       | 2026-03-27T00:00:00Z | 2026-03-27T00:00:00Z |
      | L_C_H3 | ACC3      | CUSTOMER_HOLD   | 0.00    | 1       | 2026-03-27T00:00:00Z | 2026-03-27T00:00:00Z |

  @ResetClock
  Scenario Outline: Successful cash deposit increases customer wallet balance
    Given now is "<now>"
    Given the deposit request is prepared with the following data
      | idempotencyKey   | customerId   | amount   | paymentMethod   | operatorId   |
      | <idempotencyKey> | <customerId> | <amount> | <paymentMethod> | <operatorId> |
    When a POST request has been made to "/api/finance/deposits" endpoint
    Then the response status is 201
    And the deposit response contains a transactionId
    And the following sub-ledger records were persisted in db
      | id     | accountId | ledgerType      | version | balance  |
      | L_C_W2 | ACC2      | CUSTOMER_WALLET | 2       | <amount> |
      | L_C_H2 | ACC2      | CUSTOMER_HOLD   | 2       | 0.00     |
    And the following transactions were persisted in db
      | idempotencyKey   | customerId   | amount   | paymentMethod   | operatorId   | type    | recordedAt |
      | <idempotencyKey> | <customerId> | <amount> | <paymentMethod> | <operatorId> | DEPOSIT | <now>      |
    And the following transaction records were persisted in db
      | subLedger | ledgerType      | direction | amount   |
      | L_C_W2    | CUSTOMER_WALLET | CREDIT    | <amount> |
      | L_S_CASH  | CASH            | DEBIT     | <amount> |
    And there are only 2 transaction records in db
    Examples:
      | idempotencyKey | customerId | amount | paymentMethod | operatorId | now                 |
      | IDK1           | CUS2       | 50.00  | CASH          | OP1        | 2026-03-28T10:00:00 |

  @ResetClock
  Scenario Outline: Successful cash deposit increases customer wallet balance twice if idempotency key is not reused
    Given now is "<now>"
    And the deposit request is prepared with the following data
      | idempotencyKey   | customerId   | amount   | paymentMethod   | operatorId   |
      | <idempotencyKey> | <customerId> | <amount> | <paymentMethod> | <operatorId> |
    When a POST request has been made to "/api/finance/deposits" endpoint
    Then the response status is 201
#    second deposit with different idempotency key
    Given the deposit request is prepared with the following data
      | idempotencyKey | customerId   | amount   | paymentMethod   | operatorId   |
      | IDK2           | <customerId> | <amount> | <paymentMethod> | <operatorId> |
    When a POST request has been made to "/api/finance/deposits" endpoint
    Then the response status is 201
    And the deposit response contains a transactionId
    And the following sub-ledger records were persisted in db
      | id     | accountId | ledgerType      | balance |
      | L_C_W2 | ACC2      | CUSTOMER_WALLET | 100     |
      | L_C_H2 | ACC2      | CUSTOMER_HOLD   | 0.00    |
    And the following transactions were persisted in db
      | idempotencyKey   | customerId   | amount   | paymentMethod   | operatorId   | type    | recordedAt |
      | <idempotencyKey> | <customerId> | <amount> | <paymentMethod> | <operatorId> | DEPOSIT | <now>      |
      | IDK2             | <customerId> | <amount> | <paymentMethod> | <operatorId> | DEPOSIT | <now>      |
    And there are only 2 transactions in db
    And there are only 4 transaction records in db
    Examples:
      | idempotencyKey | customerId | amount | paymentMethod | operatorId | now                 |
      | IDK1           | CUS2       | 50.00  | CASH          | OP1        | 2026-03-28T10:00:00 |

  @ResetClock
  Scenario Outline: Successful cash deposit increases customer wallet balance only once if idempotency key is reused
    Given now is "<now>"
    And the deposit request is prepared with the following data
      | idempotencyKey   | customerId   | amount   | paymentMethod   | operatorId   |
      | <idempotencyKey> | <customerId> | <amount> | <paymentMethod> | <operatorId> |
    When a POST request has been made to "/api/finance/deposits" endpoint
    Then the response status is 201
#    second deposit with the same idempotency key
    Given the deposit request is prepared with the following data
      | idempotencyKey   | customerId   | amount   | paymentMethod   | operatorId   |
      | <idempotencyKey> | <customerId> | <amount> | <paymentMethod> | <operatorId> |
    When a POST request has been made to "/api/finance/deposits" endpoint
    Then the response status is 201
    And the deposit response contains a transactionId
    And the following sub-ledger records were persisted in db
      | id     | accountId | ledgerType      | balance  |
      | L_C_W2 | ACC2      | CUSTOMER_WALLET | <amount> |
      | L_C_H2 | ACC2      | CUSTOMER_HOLD   | 0.00     |
    And the following transactions were persisted in db
      | idempotencyKey   | customerId   | amount   | paymentMethod   | operatorId   | type    | recordedAt |
      | <idempotencyKey> | <customerId> | <amount> | <paymentMethod> | <operatorId> | DEPOSIT | <now>      |
    And there are only 1 transactions in db
    And there are only 2 transaction records in db
    Examples:
      | idempotencyKey | customerId | amount | paymentMethod | operatorId | now                 |
      | IDK1           | CUS2       | 50.00  | CASH          | OP1        | 2026-03-28T10:00:00 |

  Scenario: Successful cash deposit increases customer wallet balance for parallel requests
    When a 10 POST requests have been performed to deposit CASH with 50 amount
    Then the response status is 201
    And the following sub-ledger records were persisted in db
      | id     | accountId | ledgerType      | balance |
      | L_C_W2 | ACC2      | CUSTOMER_WALLET | 500     |
      | L_C_H2 | ACC2      | CUSTOMER_HOLD   | 0.00    |
    And there are only 10 transactions in db

  Scenario Outline: Fail cash deposit increases for non-existing customer
    Given the deposit request is prepared with the following data
      | idempotencyKey   | customerId   | amount   | paymentMethod | operatorId   |
      | <idempotencyKey> | <customerId> | <amount> | CASH          | <operatorId> |
    When a POST request has been made to "/api/finance/deposits" endpoint
    Then the response status is 404
    And the response contains
      | path     | value                                                                    |
      | $.title  | Not Found                                                                |
      | $.detail | Account with identifier '11111111-1111-1111-1111-111111111114' not found |
    And the following sub-ledger records were persisted in db
      | id     | accountId | ledgerType      | balance |
      | L_C_W2 | ACC2      | CUSTOMER_WALLET | 0.00    |
      | L_C_H2 | ACC2      | CUSTOMER_HOLD   | 0.00    |
    Examples:
      | idempotencyKey | customerId | amount | operatorId |
      | IDK1           | CUS4       | 50.00  | OP1        |
