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
    And the following sub-ledger records exist in db
      | id     | accountId | ledgerType | balance | createdAt            | updatedAt            |
      | L_C_W2 | ACC2      | WALLET     | 0.00    | 2026-03-27T00:00:00Z | 2026-03-27T00:00:00Z |
      | L_C_H2 | ACC2      | HOLD       | 0.00    | 2026-03-27T00:00:00Z | 2026-03-27T00:00:00Z |


  Scenario: Successful cash deposit increases customer wallet balance
#    Given a customer is registered with phone "+79991234568" firstName "Anna" lastName "Ivanova"
#    When staff records a cash deposit of 50.00 for the customer with operator "operator-1"
#    Then the response status is 201
#
#  Scenario: Successful cash deposit increases customer wallet balance
#    Given a customer is registered with phone "+79991234568" firstName "Anna" lastName "Ivanova"
#    When staff records a cash deposit of 50.00 for the customer with operator "operator-1"
#    Then the response status is 201
#    And the deposit response contains a transactionId
#    And the customer wallet balance is increased by 50.00 in db
#    And a transaction record exists in db with type "DEPOSIT" and paymentMethod "CASH" and operatorId "operator-1"
#
#  Scenario: Deposit rejected for unknown customer
#    When a deposit request is submitted for unknown customerId "00000000-0000-0000-0000-000000000000" with amount 50.00 and paymentMethod "CASH" and operator "operator-1"
#    Then the response status is 404
#
#  Scenario: Deposit rejected for zero amount
#    Given a customer is registered with phone "+79991234569" firstName "Boris" lastName "Sidorov"
#    When a deposit request is submitted for the customer with amount 0.00 and paymentMethod "CASH" and operator "operator-1"
#    Then the response status is 400
