Feature: Fund Deposit
  As a staff member
  I want to record a customer fund deposit at the counter
  So that the customer's wallet balance is increased

  Background:
    Given the request header "Content-Type" is "application/vnd.bikerental.v1+json"
    And a customer request with the following data
      | phone        | firstName | lastName |
      | +79991234567 | Ivan      | Petrov   |
    And a POST request has been made to "/api/customers" endpoint
    And the following account record was persisted in db
      | id    | accountType | customerId |
      | ACC_S | SYSTEM      |            |
    And the following sub-ledger records were persisted in db
      | id       | accountId | ledgerType    | balance |
      | L_S_CASH | ACC_S     | CASH          | 0.00    |
      | L_S_CARD | ACC_S     | CARD_TERMINAL | 0.00    |
      | L_S_TRAN | ACC_S     | BANK_TRANSFER | 0.00    |
      | L_S_REV  | ACC_S     | REVENUE       | 0.00    |
      | L_S_ADJ  | ACC_S     | ADJUSTMENT    | 0.00    |
    And the following account records exist in db
      | id    | accountType | customerId |
      | ACC_S | SYSTEM      |            |
      | ACC2  | CUSTOMER    | CUS1       |
    And the following sub-ledger records exist in db
      | id  | accountId | ledgerType | balance | createdAt            | updatedAt            |
      | SL1 | ACC1      | CASH       | 0.00    | 2026-03-27T00:00:00Z | 2026-03-27T00:00:00Z |
      | SL2 | ACC1      | REVENUE    | 0.00    | 2026-03-27T00:00:00Z | 2026-03-27T00:00:00Z |

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
