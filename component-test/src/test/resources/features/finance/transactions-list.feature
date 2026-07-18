Feature: Cross-customer transaction listing
  As a back-office administrator
  I want to list business transactions across customers with their full double-entry breakdown
  So that I can audit both customer and system-side money movement

  Background:
    Given the request header "Content-Type" is "application/vnd.bikerental.v1+json"
    And customers exist in the database with the following data
      | id   | phone       | firstName | lastName | email            | birthDate  |
      | CUS2 | +3706861555 | John      | Doe      | john@example.com | 1922-02-22 |
      | CUS3 | +3752951111 | Alex      | Johnson  | null             | null       |
    And the following account records exist in db
      | id   | accountType | customerId |
      | ACC2 | CUSTOMER    | CUS2       |
      | ACC3 | CUSTOMER    | CUS3       |
    And the following sub-ledger records exist in db
      | id     | accountId | ledgerType      | balance | version | createdAt            | updatedAt            |
      | L_C_W2 | ACC2      | CUSTOMER_WALLET | 200.00  | 5       | 2026-01-01T00:00:00Z | 2026-04-07T10:00:00Z |
      | L_C_H2 | ACC2      | CUSTOMER_HOLD   | 0.00    | 1       | 2026-01-01T00:00:00Z | 2026-01-01T00:00:00Z |
      | L_C_W3 | ACC3      | CUSTOMER_WALLET | 0.00    | 0       | 2026-01-01T00:00:00Z | 2026-04-07T10:00:00Z |
      | L_C_H3 | ACC3      | CUSTOMER_HOLD   | 0.00    | 1       | 2026-01-01T00:00:00Z | 2026-01-01T00:00:00Z |
    And the following transaction records exist in db
      | id  | type    | paymentMethod | amount | customerId | operatorId | sourceType | sourceId | recordedAt          | idempotencyKey |
      | TX1 | DEPOSIT | CASH          | 50.00  | CUS2       | OP1        |            |          | 2026-01-10T10:00:00 | IDK1           |
      | TX2 | DEPOSIT | CASH          | 60.00  | CUS2       | OP1        |            |          | 2026-02-15T10:00:00 | IDK2           |
      | TX3 | CAPTURE | CASH          | 70.00  | CUS2       | OP1        | RENTAL     | RENT1    | 2026-03-20T10:00:00 | IDK3           |
      | TX4 | HOLD    | CASH          | 10.00  | CUS2       | OP1        | RENTAL     | RENT2    | 2026-03-21T10:00:00 | IDK4           |
      | TX9 | DEPOSIT | CASH          | 80.00  | CUS3       | OP1        |            |          | 2026-04-01T10:00:00 | IDK9           |
    And the following transaction record entries exist in db
      | id    | transaction | subLedger | ledgerType      | direction | amount |
      | TRE1  | TX1         | L_C_W2    | CUSTOMER_WALLET | CREDIT    | 50.00  |
      | TRE2  | TX1         | L_S_CASH  | CASH            | DEBIT     | 50.00  |
      | TRE3  | TX2         | L_C_W2    | CUSTOMER_WALLET | CREDIT    | 60.00  |
      | TRE4  | TX2         | L_S_CASH  | CASH            | DEBIT     | 60.00  |
      | TRE5  | TX3         | L_C_H2    | CUSTOMER_HOLD   | DEBIT     | 70.00  |
      | TRE6  | TX3         | L_S_REV   | REVENUE         | CREDIT    | 70.00  |
      | TRE7  | TX4         | L_C_W2    | CUSTOMER_WALLET | DEBIT     | 10.00  |
      | TRE8  | TX4         | L_C_H2    | CUSTOMER_HOLD   | CREDIT    | 10.00  |
      | TRE11 | TX9         | L_C_W3    | CUSTOMER_WALLET | CREDIT    | 80.00  |
      | TRE12 | TX9         | L_S_CASH  | CASH            | DEBIT     | 80.00  |

  Scenario: Unfiltered query returns all customers' transactions with both customer and system legs
    When a GET request has been made to "/api/finance/transactions" endpoint
    Then the response status is 200
    And the response contains
      | path               | value |
      | $.totalItems       | 5     |
      | $.pageRequest.page | 0     |
      | $.pageRequest.size | 20    |
    And the transactions response only contains
      | customerId | type    | amount | sourceType | sourceId | ledgerTypes             |
      | CUS2       | DEPOSIT | 50.00  |            |          | CASH,CUSTOMER_WALLET    |
      | CUS2       | DEPOSIT | 60.00  |            |          | CASH,CUSTOMER_WALLET    |
      | CUS2       | CAPTURE | 70.00  | RENTAL     | RENT1    | REVENUE,CUSTOMER_HOLD   |
      | CUS2       | HOLD    | 10.00  | RENTAL     | RENT2    | CUSTOMER_WALLET,CUSTOMER_HOLD |
      | CUS3       | DEPOSIT | 80.00  |            |          | CASH,CUSTOMER_WALLET    |
    And the transactions are ordered by recordedAt descending

  Scenario: Filter by a single customer returns only that customer's transactions
    When a GET request has been made to "/api/finance/transactions" endpoint with query parameters
      | customerIds |
      | CUS3        |
    Then the response status is 200
    And the response contains
      | path         | value |
      | $.totalItems | 1     |
    And the transactions response only contains
      | customerId | type    | amount | ledgerTypes          |
      | CUS3       | DEPOSIT | 80.00  | CASH,CUSTOMER_WALLET |

  Scenario: Filter by a list of customers returns transactions for any of them
    When a GET request has been made to "/api/finance/transactions" endpoint with query parameters
      | customerIds |
      | CUS2,CUS3   |
    Then the response status is 200
    And the response contains
      | path         | value |
      | $.totalItems | 5     |

  Scenario: Filter by a system ledger type returns only transactions touching that ledger
    When a GET request has been made to "/api/finance/transactions" endpoint with query parameters
      | ledgerTypes |
      | REVENUE     |
    Then the response status is 200
    And the response contains
      | path         | value |
      | $.totalItems | 1     |
    And the transactions response only contains
      | type    | amount | sourceType | sourceId | ledgerTypes           |
      | CAPTURE | 70.00  | RENTAL     | RENT1    | REVENUE,CUSTOMER_HOLD |

  Scenario: Filter by sourceType returns only matching transactions
    When a GET request has been made to "/api/finance/transactions" endpoint with query parameters
      | sourceType |
      | RENTAL     |
    Then the response status is 200
    And the response contains
      | path         | value |
      | $.totalItems | 2     |
    And the transactions response only contains
      | type    | amount | sourceType | sourceId |
      | CAPTURE | 70.00  | RENTAL     | RENT1    |
      | HOLD    | 10.00  | RENTAL     | RENT2    |

  Scenario: Filter by recorded-at date range returns only transactions within the range
    When a GET request has been made to "/api/finance/transactions" endpoint with query parameters
      | fromDate   | toDate     |
      | 2026-02-01 | 2026-02-28 |
    Then the response status is 200
    And the response contains
      | path         | value |
      | $.totalItems | 1     |
    And the transactions response only contains
      | type    | amount |
      | DEPOSIT | 60.00  |

  Scenario: A HOLD transaction with two customer legs is listed exactly once
    When a GET request has been made to "/api/finance/transactions" endpoint with query parameters
      | customerIds |
      | CUS2        |
    Then the response status is 200
    And the response contains
      | path         | value |
      | $.totalItems | 4     |
    And the transactions response only contains
      | customerId | type    | amount | ledgerTypes                   |
      | CUS2       | DEPOSIT | 50.00  | CASH,CUSTOMER_WALLET          |
      | CUS2       | DEPOSIT | 60.00  | CASH,CUSTOMER_WALLET          |
      | CUS2       | CAPTURE | 70.00  | REVENUE,CUSTOMER_HOLD         |
      | CUS2       | HOLD    | 10.00  | CUSTOMER_WALLET,CUSTOMER_HOLD |

  Scenario: Query for a customer with no transactions returns an empty page
    When a GET request has been made to "/api/finance/transactions" endpoint with query parameters
      | customerIds |
      | CUS4        |
    Then the response status is 200
    And the response contains
      | path         | value |
      | $.totalItems | 0     |
