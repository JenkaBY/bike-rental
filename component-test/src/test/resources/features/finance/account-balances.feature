Feature: Customer account balance retrieval
  As a staff member
  I want to retrieve a customer's current wallet and hold balances
  So that I can advise the customer on available funds before any financial operation

  Background:
    Given the request header "Content-Type" is "application/vnd.bikerental.v1+json"
    And customers exist in the database with the following data
      | id   | phone       | firstName | lastName | email            | birthDate  |
      | CUS2 | +3706861555 | John      | Doe      | john@example.com | 1922-02-22 |
      | CUS3 | +3706861551 | Jane      | Doe      | jane@example.com | 1922-02-21 |
    And the following account records exist in db
      | id   | accountType | customerId |
      | ACC2 | CUSTOMER    | CUS2       |
      | ACC3 | CUSTOMER    | CUS3       |
    And the following sub-ledger records exist in db
      | id     | accountId | ledgerType      | balance | version | createdAt            | updatedAt            |
      | L_C_W2 | ACC2      | CUSTOMER_WALLET | 120.00  | 2       | 2026-03-27T00:00:00Z | 2026-04-07T10:31:02Z |
      | L_C_H2 | ACC2      | CUSTOMER_HOLD   | 30.00   | 2       | 2026-03-27T00:00:00Z | 2026-04-07T10:30:00Z |
      | L_C_W3 | ACC3      | CUSTOMER_WALLET | 80.00   | 1       | 2026-03-27T00:00:00Z | 2026-03-27T00:00:00Z |
      | L_C_H3 | ACC3      | CUSTOMER_HOLD   | 20.00   | 1       | 2026-03-27T00:00:00Z | 2026-03-27T00:00:00Z |

  Scenario: Retrieve balances for a customer with wallet funds and an active hold
    When a GET request has been made to "/api/finance/customers/{customerId}/balances" endpoint with
      | {customerId} |
      | CUS2         |
    Then the response status is 200
    And the balances response contains
      | walletBalance | holdBalance |
      | 120.00        | 30.00       |

  Scenario: Retrieve balances for a customer with no transactions returns zero balances
    Given a customer request with the following data
      | phone       | firstName | lastName |
      | +3701234567 | Ivan      | Petrov   |
    And a POST request has been made to "/api/customers" endpoint
    And the response status is 201
    When customerId is extracted from the response and stored as 'requestedObjectId'
    When a GET request has been made to "/api/finance/customers/{requestedObjectId}/balances" endpoint with context
    Then the response status is 200
    And the balances response contains
      | walletBalance | holdBalance |
      | 0.00          | 0.00        |
