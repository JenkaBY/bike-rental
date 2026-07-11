Feature: Customer transaction history retrieval
  As a staff member
  I want to retrieve a paginated list of all financial entries for a customer
  So that I can audit all financial activity for that customer

  Background:
    Given the request header "Content-Type" is "application/vnd.bikerental.v1+json"
    And customers exist in the database with the following data
      | id   | phone       | firstName | lastName | email            | birthDate  |
      | CUS3 | +3752951111 | Alex      | Johnson  | null             | null       |
      | CUS2 | +3706861555 | John      | Doe      | john@example.com | 1922-02-22 |
    And the following account records exist in db
      | id   | accountType | customerId |
      | ACC3 | CUSTOMER    | CUS3       |
      | ACC2 | CUSTOMER    | CUS2       |
    And the following sub-ledger records exist in db
      | id     | accountId | ledgerType      | balance | version | createdAt            | updatedAt            |
      | L_C_W3 | ACC3      | CUSTOMER_WALLET | 0.00    | 0       | 2026-01-01T00:00:00Z | 2026-04-07T10:00:00Z |
      | L_C_H3 | ACC3      | CUSTOMER_HOLD   | 0.00    | 1       | 2026-01-01T00:00:00Z | 2026-01-01T00:00:00Z |
      | L_C_W2 | ACC2      | CUSTOMER_WALLET | 200.00  | 5       | 2026-01-01T00:00:00Z | 2026-04-07T10:00:00Z |
      | L_C_H2 | ACC2      | CUSTOMER_HOLD   | 0.00    | 1       | 2026-01-01T00:00:00Z | 2026-01-01T00:00:00Z |
    And the following transaction records exist in db
      | id  | type    | paymentMethod | amount | customerId | operatorId | sourceType | sourceId | recordedAt          | idempotencyKey |
      | TX1 | DEPOSIT | CASH          | 50.00  | CUS2       | OP1        |            |          | 2026-01-10T10:00:00 | IDK1           |
      | TX2 | DEPOSIT | CASH          | 60.00  | CUS2       | OP1        |            |          | 2026-02-15T10:00:00 | IDK2           |
      | TX3 | CAPTURE | CASH          | 70.00  | CUS2       | OP1        | RENTAL     | RENT1    | 2026-03-20T10:00:00 | IDK3           |
      | TX4 | HOLD    | CASH          | 10.00  | CUS2       | OP1        | RENTAL     | RENT2    | 2026-03-21T10:00:00 | IDK4           |
    And the following transaction record entries exist in db
      | id   | transaction | subLedger | ledgerType      | direction | amount |
      | TRE1 | TX1         | L_C_W2    | CUSTOMER_WALLET | CREDIT    | 50.00  |
      | TRE2 | TX1         | L_S_CASH  | CASH            | DEBIT     | 50.00  |
      | TRE3 | TX2         | L_C_W2    | CUSTOMER_WALLET | CREDIT    | 60.00  |
      | TRE4 | TX2         | L_S_CASH  | CASH            | DEBIT     | 60.00  |
      | TRE5 | TX3         | L_C_H2    | CUSTOMER_HOLD   | DEBIT     | 70.00  |
      | TRE6 | TX3         | L_S_REV   | REVENUE         | CREDIT    | 70.00  |

  Scenario: Paginated query returns entries in reverse-chronological order
    When a GET request has been made to "/api/finance/customers/{customerId}/transactions" endpoint with
      | {customerId} |
      | CUS2         |
    Then the response status is 200
    And the response contains
      | path               | value |
      | $.totalItems       | 3     |
      | $.pageRequest.page | 0     |
      | $.pageRequest.size | 20    |
    And the transaction history response only contains entries of
      | subLedger       | amount | direction | type    | sourceType | sourceId | recordedAt          |
      | CUSTOMER_HOLD   | 70.00  | DEBIT     | CAPTURE |            |          | 2026-03-20T10:00:00 |
      | CUSTOMER_WALLET | 60.00  | CREDIT    | DEPOSIT |            |          | 2026-02-15T10:00:00 |
      | CUSTOMER_WALLET | 50.00  | CREDIT    | DEPOSIT |            |          | 2026-01-10T10:00:00 |

  Scenario: Query for customer with no transactions returns empty result
    When a GET request has been made to "/api/finance/customers/{customerId}/transactions" endpoint with
      | {customerId} |
      | CUS3         |
    Then the response status is 200
    And the response contains
      | path               | value |
      | $.totalItems       | 0     |
      | $.pageRequest.page | 0     |
      | $.pageRequest.size | 20    |

  Scenario: Query rejected for unknown customer returns 404
    When a GET request has been made to "/api/finance/customers/{customerId}/transactions" endpoint with
      | {customerId}                         |
      | 00000000-0000-0000-0000-000000000099 |
    Then the response status is 404

  Scenario: Filter by date range returns only entries within the range
    When a GET request has been made to "/api/finance/customers/{customerId}/transactions" endpoint with query parameters
      | {customerId} | fromDate   | toDate     |
      | CUS2         | 2026-02-01 | 2026-02-28 |
    Then the response status is 200
    And the response contains
      | path               | value |
      | $.totalItems       | 1     |
      | $.pageRequest.page | 0     |
      | $.pageRequest.size | 20    |
    And the transaction history response only contains entries of
      | amount | type    | sourceType | sourceId | recordedAt          |
      | 60.00  | DEPOSIT |            |          | 2026-02-15T10:00:00 |

  Scenario: Filter by sourceType returns only matching entries
    When a GET request has been made to "/api/finance/customers/{customerId}/transactions" endpoint with query parameters
      | {customerId} | sourceType |
      | CUS2         | RENTAL     |
    Then the response status is 200
    And the response contains
      | path               | value |
      | $.totalItems       | 1     |
      | $.pageRequest.page | 0     |
      | $.pageRequest.size | 20    |
    And the transaction history response only contains entries of
      | id  | amount | type    | sourceType | sourceId |
      | TR3 | 70.00  | CAPTURE | RENTAL     | RENT1    |

  Scenario: Filter by sourceId returns only entries linked to that source
    When a GET request has been made to "/api/finance/customers/{customerId}/transactions" endpoint with query parameters
      | {customerId} | sourceId |
      | CUS2         | RENT1    |
    Then the response status is 200
    And the response contains
      | path               | value |
      | $.totalItems       | 1     |
      | $.pageRequest.page | 0     |
      | $.pageRequest.size | 20    |
    And the transaction history response only contains entries of
      | id  | amount | direction | type    | sourceType | sourceId |
      | TR3 | 70.00  | DEBIT     | CAPTURE | RENTAL     | RENT1    |

  Scenario: Combined filters apply AND logic
    When a GET request has been made to "/api/finance/customers/{customerId}/transactions" endpoint with query parameters
      | {customerId} | fromDate   | toDate     | sourceType |
      | CUS2         | 2026-02-01 | 2026-03-28 | RENTAL     |
    Then the response status is 200
    And the response contains
      | path               | value |
      | $.totalItems       | 1     |
      | $.pageRequest.page | 0     |
      | $.pageRequest.size | 20    |
    And the transaction history response only contains entries of
      | id  | amount | direction | type    | sourceType | sourceId |
      | TR3 | 70.00  | DEBIT     | CAPTURE | RENTAL     | RENT1    |

  Scenario: Same-day range honors business timezone near local midnight
    Given the following transaction records exist in db
      | id  | type    | paymentMethod | amount | customerId | operatorId | sourceType | sourceId | recordedAt           | idempotencyKey |
      | TX5 | DEPOSIT | CASH          | 11.00  | CUS2       | OP1        |            |          | 2026-02-07T21:00:00Z | IDK5           |
      | TX6 | DEPOSIT | CASH          | 12.00  | CUS2       | OP1        |            |          | 2026-02-08T10:00:00Z | IDK6           |
      | TX7 | DEPOSIT | CASH          | 13.00  | CUS2       | OP1        |            |          | 2026-02-08T20:59:59Z | IDK7           |
      | TX8 | DEPOSIT | CASH          | 14.00  | CUS2       | OP1        |            |          | 2026-02-08T21:00:00Z | IDK8           |
    And the following transaction record entries exist in db
      | id    | transaction | subLedger | ledgerType      | direction | amount |
      | TRE7  | TX5         | L_C_W2    | CUSTOMER_WALLET | CREDIT    | 11.00  |
      | TRE8  | TX6         | L_C_W2    | CUSTOMER_WALLET | CREDIT    | 12.00  |
      | TRE9  | TX7         | L_C_W2    | CUSTOMER_WALLET | CREDIT    | 13.00  |
      | TRE10 | TX8         | L_C_W2    | CUSTOMER_WALLET | CREDIT    | 14.00  |
    When a GET request has been made to "/api/finance/customers/{customerId}/transactions" endpoint with query parameters
      | {customerId} | fromDate   | toDate     |
      | CUS2         | 2026-02-08 | 2026-02-08 |
    Then the response status is 200
    And the response contains
      | path               | value |
      | $.totalItems       | 3     |
      | $.pageRequest.page | 0     |
      | $.pageRequest.size | 20    |
    And the transaction history response only contains entries of
      | amount | type    | recordedAt          |
      | 13.00  | DEPOSIT | 2026-02-08T20:59:59 |
      | 12.00  | DEPOSIT | 2026-02-08T10:00:00 |
      | 11.00  | DEPOSIT | 2026-02-07T21:00:00 |

  Scenario: Rental flow exposes per-bucket deltas, direction and running balances
    Given the following transaction records exist in db
      | id   | type    | paymentMethod     | amount | customerId | operatorId | sourceType | sourceId | recordedAt          | idempotencyKey |
      | TX9  | DEPOSIT | CASH              | 100.00 | CUS3       | OP1        |            |          | 2026-05-01T10:00:00 | IDK9           |
      | TX10 | HOLD    | INTERNAL_TRANSFER | 18.00  | CUS3       | OP1        | RENTAL     | RENT9    | 2026-05-01T11:00:00 | IDK10          |
      | TX11 | RELEASE | INTERNAL_TRANSFER | 13.00  | CUS3       | OP1        | RENTAL     | RENT9    | 2026-05-01T12:00:00 | IDK11          |
      | TX12 | CAPTURE | INTERNAL_TRANSFER | 5.00   | CUS3       | OP1        | RENTAL     | RENT9    | 2026-05-01T13:00:00 | IDK12          |
    And the following transaction record entries exist in db
      | id    | transaction | subLedger | ledgerType      | direction | amount | runningBalance |
      | TRE11 | TX9         | L_C_W3    | CUSTOMER_WALLET | CREDIT    | 100.00 | 100.00       |
      | TRE12 | TX9         | L_S_CASH  | CASH            | DEBIT     | 100.00 |              |
      | TRE13 | TX10        | L_C_W3    | CUSTOMER_WALLET | DEBIT     | 18.00  | 82.00        |
      | TRE14 | TX10        | L_C_H3    | CUSTOMER_HOLD   | CREDIT    | 18.00  | 18.00        |
      | TRE15 | TX11        | L_C_H3    | CUSTOMER_HOLD   | DEBIT     | 13.00  | 5.00         |
      | TRE16 | TX11        | L_C_W3    | CUSTOMER_WALLET | CREDIT    | 13.00  | 95.00        |
      | TRE17 | TX12        | L_C_H3    | CUSTOMER_HOLD   | DEBIT     | 5.00   | 0.00         |
      | TRE18 | TX12        | L_S_REV   | REVENUE         | CREDIT    | 5.00   |              |
    When a GET request has been made to "/api/finance/customers/{customerId}/transactions" endpoint with
      | {customerId} |
      | CUS3         |
    Then the response status is 200
    And the response contains
      | path               | value |
      | $.totalItems       | 4     |
      | $.pageRequest.size | 20    |
    And the transaction history response only contains entries of
      | type    | amount | direction | walletDelta | holdDelta | externalDelta | walletBalanceAfter | holdBalanceAfter | recordedAt          |
      | CAPTURE | 5.00   | DEBIT     | 0.00        | -5.00     | -5.00         | 95.00              | 0.00             | 2026-05-01T13:00:00 |
      | RELEASE | 13.00  | CREDIT    | 13.00       | -13.00    | 0.00          | 95.00              | 5.00             | 2026-05-01T12:00:00 |
      | HOLD    | 18.00  | DEBIT     | -18.00      | 18.00     | 0.00          | 82.00              | 18.00            | 2026-05-01T11:00:00 |
      | DEPOSIT | 100.00 | CREDIT    | 100.00      | 0.00      | 100.00        | 100.00             | 0.00             | 2026-05-01T10:00:00 |

  Scenario: Adjustment deduction is reported as a DEBIT with a negative wallet delta
    Given the following transaction records exist in db
      | id   | type       | paymentMethod     | amount | customerId | operatorId | recordedAt          | idempotencyKey | reason        |
      | TX13 | ADJUSTMENT | INTERNAL_TRANSFER | 20.00  | CUS3       | OP1        | 2026-06-01T10:00:00 | IDK13          | manual charge |
    And the following transaction record entries exist in db
      | id    | transaction | subLedger | ledgerType      | direction | amount | runningBalance |
      | TRE19 | TX13        | L_C_W3    | CUSTOMER_WALLET | DEBIT     | 20.00  | -20.00       |
      | TRE20 | TX13        | L_S_ADJ   | ADJUSTMENT      | CREDIT    | 20.00  |              |
    When a GET request has been made to "/api/finance/customers/{customerId}/transactions" endpoint with
      | {customerId} |
      | CUS3         |
    Then the response status is 200
    And the transaction history response only contains entries of
      | type       | amount | direction | walletDelta | holdDelta | externalDelta | reason        |
      | ADJUSTMENT | 20.00  | DEBIT     | -20.00      | 0.00      | -20.00        | manual charge |
