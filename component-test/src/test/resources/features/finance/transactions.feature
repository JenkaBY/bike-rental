Feature: Finance transactions
  As a back-office administrator
  I want to list business transactions across customers with their full double-entry breakdown,
  and retrieve any single transaction's full details
  So that I can audit both customer and system-side money movement, and explain exactly how money
  moved on a specific transaction

  Background:
    Given the request header "Content-Type" is "application/vnd.bikerental.v1+json"
    And customers exist in the database with the following data
      | id   | phone       | firstName | lastName | email             | birthDate  |
      | CUS2 | +3706861555 | John      | Doe      | john@example.com  | 1922-02-22 |
      | CUS3 | +3752951111 | Alex      | Johnson  | null              | null       |
      | CUS5 | +3706999999 | Maria     | Petrova  | maria@example.com | 1990-05-05 |
    And the following account records exist in db
      | id   | accountType | customerId |
      | ACC2 | CUSTOMER    | CUS2       |
      | ACC3 | CUSTOMER    | CUS3       |
      | ACC5 | CUSTOMER    | CUS5       |
    And the following sub-ledger records exist in db
      | id     | accountId | ledgerType      | balance | version | createdAt            | updatedAt            |
      | L_C_W2 | ACC2      | CUSTOMER_WALLET | 200.00  | 5       | 2026-01-01T00:00:00Z | 2026-04-07T10:00:00Z |
      | L_C_H2 | ACC2      | CUSTOMER_HOLD   | 0.00    | 1       | 2026-01-01T00:00:00Z | 2026-01-01T00:00:00Z |
      | L_C_W3 | ACC3      | CUSTOMER_WALLET | 0.00    | 0       | 2026-01-01T00:00:00Z | 2026-04-07T10:00:00Z |
      | L_C_H3 | ACC3      | CUSTOMER_HOLD   | 0.00    | 1       | 2026-01-01T00:00:00Z | 2026-01-01T00:00:00Z |
      | L_C_W5 | ACC5      | CUSTOMER_WALLET | 40.00   | 2       | 2026-05-01T00:00:00Z | 2026-05-03T10:00:00Z |
      | L_C_H5 | ACC5      | CUSTOMER_HOLD   | 0.00    | 2       | 2026-05-01T00:00:00Z | 2026-05-03T10:00:00Z |
    And the following transaction records exist in db
      | id  | type    | paymentMethod | amount | customerId | operatorId | sourceType | sourceId | recordedAt          | idempotencyKey |
      | TX1 | DEPOSIT | CASH          | 50.00  | CUS2       | OP1        |            |          | 2026-01-10T10:00:00 | IDK1           |
      | TX2 | DEPOSIT | CASH          | 60.00  | CUS2       | OP1        |            |          | 2026-02-15T10:00:00 | IDK2           |
      | TX3 | CAPTURE | CASH          | 70.00  | CUS2       | OP1        | RENTAL     | RENT1    | 2026-03-20T10:00:00 | IDK3           |
      | TX4 | HOLD    | CASH          | 10.00  | CUS2       | OP1        | RENTAL     | RENT2    | 2026-03-21T10:00:00 | IDK4           |
      | TX9 | DEPOSIT | CASH          | 80.00  | CUS3       | OP1        |            |          | 2026-04-01T10:00:00 | IDK9           |
      | TX5 | DEPOSIT | CASH          | 50.00  | CUS5       | OP1        |            |          | 2026-05-01T10:00:00 | IDK5           |
      | TX6 | HOLD    | CASH          | 10.00  | CUS5       | OP1        | RENTAL     | RENT1    | 2026-05-02T10:00:00 | IDK6           |
      | TX7 | CAPTURE | CASH          | 10.00  | CUS5       | OP1        | RENTAL     | RENT1    | 2026-05-03T10:00:00 | IDK7           |
    And the following transaction record entries exist in db
      | id    | transaction | subLedger | ledgerType      | direction | amount | runningBalance |
      | TRE1  | TX1         | L_C_W2    | CUSTOMER_WALLET | CREDIT    | 50.00  |                |
      | TRE2  | TX1         | L_S_CASH  | CASH            | DEBIT     | 50.00  |                |
      | TRE3  | TX2         | L_C_W2    | CUSTOMER_WALLET | CREDIT    | 60.00  |                |
      | TRE4  | TX2         | L_S_CASH  | CASH            | DEBIT     | 60.00  |                |
      | TRE5  | TX3         | L_C_H2    | CUSTOMER_HOLD   | DEBIT     | 70.00  |                |
      | TRE6  | TX3         | L_S_REV   | REVENUE         | CREDIT    | 70.00  |                |
      | TRE7  | TX4         | L_C_W2    | CUSTOMER_WALLET | DEBIT     | 10.00  |                |
      | TRE8  | TX4         | L_C_H2    | CUSTOMER_HOLD   | CREDIT    | 10.00  |                |
      | TRE11 | TX9         | L_C_W3    | CUSTOMER_WALLET | CREDIT    | 80.00  |                |
      | TRE12 | TX9         | L_S_CASH  | CASH            | DEBIT     | 80.00  |                |
      | TRE13 | TX5         | L_C_W5    | CUSTOMER_WALLET | CREDIT    | 50.00  | 50.00          |
      | TRE14 | TX5         | L_S_CASH  | CASH            | DEBIT     | 50.00  | 50.00          |
      | TRE15 | TX6         | L_C_W5    | CUSTOMER_WALLET | DEBIT     | 10.00  | 40.00          |
      | TRE16 | TX6         | L_C_H5    | CUSTOMER_HOLD   | CREDIT    | 10.00  | 10.00          |
      | TRE17 | TX7         | L_C_H5    | CUSTOMER_HOLD   | DEBIT     | 10.00  | 0.00           |
      | TRE18 | TX7         | L_S_REV   | REVENUE         | CREDIT    | 10.00  | 10.00          |

  Scenario: Unfiltered query returns all customers' transactions with both customer and system legs
    When a GET request has been made to "/api/finance/transactions" endpoint
    Then the response status is 200
    And the response contains
      | path               | value |
      | $.totalItems       | 8     |
      | $.pageRequest.page | 0     |
      | $.pageRequest.size | 20    |
    And the transactions response only contains
      | customerId | type    | amount | sourceType | sourceId | ledgerTypes                   |
      | CUS2       | DEPOSIT | 50.00  |            |          | CASH,CUSTOMER_WALLET          |
      | CUS2       | DEPOSIT | 60.00  |            |          | CASH,CUSTOMER_WALLET          |
      | CUS2       | CAPTURE | 70.00  | RENTAL     | RENT1    | REVENUE,CUSTOMER_HOLD         |
      | CUS2       | HOLD    | 10.00  | RENTAL     | RENT2    | CUSTOMER_WALLET,CUSTOMER_HOLD |
      | CUS3       | DEPOSIT | 80.00  |            |          | CASH,CUSTOMER_WALLET          |
      | CUS5       | DEPOSIT | 50.00  |            |          | CASH,CUSTOMER_WALLET          |
      | CUS5       | HOLD    | 10.00  | RENTAL     | RENT1    | CUSTOMER_WALLET,CUSTOMER_HOLD |
      | CUS5       | CAPTURE | 10.00  | RENTAL     | RENT1    | REVENUE,CUSTOMER_HOLD         |
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
      | $.totalItems | 2     |
    And the transactions response only contains
      | type    | amount | sourceType | sourceId | ledgerTypes           |
      | CAPTURE | 70.00  | RENTAL     | RENT1    | REVENUE,CUSTOMER_HOLD |
      | CAPTURE | 10.00  | RENTAL     | RENT1    | REVENUE,CUSTOMER_HOLD |

  Scenario: Filter by sourceType returns only matching transactions
    When a GET request has been made to "/api/finance/transactions" endpoint with query parameters
      | sourceType |
      | RENTAL     |
    Then the response status is 200
    And the response contains
      | path         | value |
      | $.totalItems | 4     |
    And the transactions response only contains
      | type    | amount | sourceType | sourceId |
      | CAPTURE | 70.00  | RENTAL     | RENT1    |
      | HOLD    | 10.00  | RENTAL     | RENT2    |
      | HOLD    | 10.00  | RENTAL     | RENT1    |
      | CAPTURE | 10.00  | RENTAL     | RENT1    |

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

  Scenario: A deposit exposes both the customer wallet leg and the system cash leg
    When a GET request has been made to "/api/finance/transactions/{transactionId}" endpoint with
      | {transactionId} |
      | TX5             |
    Then the response status is 200
    And the transaction details response contains
      | customerId | type    | amount | walletDelta | holdDelta | externalDelta | walletBalance | holdBalance |
      | CUS5       | DEPOSIT | 50.00  | 50.00       | 0.00      | 50.00         | 50.00         | 0.00        |
    And the transaction details entries only contain
      | ledgerType      | direction | amount | signedDelta | balanceAfter | systemLedger |
      | CUSTOMER_WALLET | CREDIT    | 50.00  | 50.00       | 50.00        | false        |
      | CASH            | DEBIT     | 50.00  | 50.00       | 50.00        | true         |

  Scenario: A hold moves money between the two customer buckets and nets to zero externally
    When a GET request has been made to "/api/finance/transactions/{transactionId}" endpoint with
      | {transactionId} |
      | TX6             |
    Then the response status is 200
    And the transaction details response contains
      | customerId | type | amount | walletDelta | holdDelta | externalDelta | walletBalance | holdBalance |
      | CUS5       | HOLD | 10.00  | -10.00      | 10.00     | 0.00          | 40.00         | 10.00       |
    And the transaction details entries only contain
      | ledgerType      | direction | amount | signedDelta | balanceAfter | systemLedger |
      | CUSTOMER_WALLET | DEBIT     | 10.00  | -10.00      | 40.00        | false        |
      | CUSTOMER_HOLD   | CREDIT    | 10.00  | 10.00       | 10.00        | false        |

  Scenario: A capture releases the hold into revenue and carries the wallet balance forward
    When a GET request has been made to "/api/finance/transactions/{transactionId}" endpoint with
      | {transactionId} |
      | TX7             |
    Then the response status is 200
    And the transaction details response contains
      | customerId | type    | amount | walletDelta | holdDelta | externalDelta | walletBalance | holdBalance |
      | CUS5       | CAPTURE | 10.00  | 0.00        | -10.00    | -10.00        | 40.00         | 0.00        |
    And the transaction details entries only contain
      | ledgerType    | direction | amount | signedDelta | balanceAfter | systemLedger |
      | CUSTOMER_HOLD | DEBIT     | 10.00  | -10.00      | 0.00         | false        |
      | REVENUE       | CREDIT    | 10.00  | 10.00       | 10.00        | true         |

  Scenario: An unknown transaction id returns 404
    When a GET request has been made to "/api/finance/transactions/{transactionId}" endpoint with
      | {transactionId}                      |
      | 00000000-0000-0000-0000-000000000099 |
    Then the response status is 404
    And the response contains
      | path                  | value                     |
      | $.errorCode           | shared.resource.not_found |
      | $.params.resourceName | Transaction               |
