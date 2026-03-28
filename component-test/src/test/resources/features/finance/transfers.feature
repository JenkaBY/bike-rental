Feature: Money movement between accounts
  As a staff member
  I want to record all money movements between accounts, including deposits, withdrawals, and transfers
  So that the financial records are accurate and up-to-date

  Background:
    Given the request header "Content-Type" is "application/vnd.bikerental.v1+json"
    And customers exist in the database with the following data
      | id   | phone       | firstName | lastName | email            | birthDate  | comments |
      | CUS2 | +3706861555 | John      | Doe      | john@example.com | 1922-02-22 | null     |
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
      | idempotencyKey   | customerId   | amount   | paymentMethod | operatorId   |
      | <idempotencyKey> | <customerId> | <amount> | CASH          | <operatorId> |
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

