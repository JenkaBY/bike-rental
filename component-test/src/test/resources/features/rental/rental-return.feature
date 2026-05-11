@ReinitializeSystemLedgers @ResetClock
Feature: Equipment Return
  As an operator
  I want to return rented equipment
  So that I can complete a rental and track equipment availability

  Background:
    Given the request header "Content-Type" is "application/vnd.bikerental.v1+json"
    And customers exist in the database with the following data
      | id   | phone        | firstName | lastName | email            | birthDate  | comments |
      | CUS1 | +79995551111 | Alex      | Johnson  | null             | null       | null     |
      | CUS2 | +3706861555  | John      | Doe      | john@example.com | 1922-02-22 | null     |
      | CUS3 | +3706861551  | Jane      | Doe      | jane@example.com | 1922-02-21 | null     |
    And the following equipment statues exist in the database
      | slug        | name        | description       | transitions               |
      | BROKEN      | Broken      | Not Ready to rent | AVAILABLE,MAINTENANCE     |
      | AVAILABLE   | Available   | Ready to rent     | BROKEN,MAINTENANCE,RENTED |
      | MAINTENANCE | Maintenance | null              | AVAILABLE                 |
      | RENTED      | Rented      | In use already    | AVAILABLE,BROKEN          |
    And the following equipment types exist in the database
      | slug    | name    | description |
      | BICYCLE | Bicycle | Two-wheeled |
    And the following equipment records exist in db
      | id | serialNumber | uid      | status | type    | model   | conditionNotes | condition |
      | 1  | EQ-001       | BIKE-001 | RENTED | BICYCLE | Model A | Good           | GOOD      |
      | 2  | EQ-002       | BIKE-002 | RENTED | BICYCLE | Model A | Good           | GOOD      |
    And the pricing params list for tariff request is
      | tariffId | pricingType       | firstHourPrice | hourlyDiscount | minimumHourlyPrice | hourlyPrice | dailyPrice | overtimeHourlyPrice | issuanceFee | minimumDurationMinutes | minimumDurationSurcharge | price |
      | 1        | DEGRESSIVE_HOURLY | 9.00           | 2.00           | 1.00               |             |            |                     |             | 30                     | 1.00                     |       |
      | 2        | FLAT_HOURLY       |                |                |                    | 15.00       |            |                     |             | 30                     | 1.00                     |       |
      | 3        | DAILY             |                |                |                    |             | 25.00      | 1.00                |             |                        |                          |       |
      | 4        | FLAT_FEE          |                |                |                    |             |            |                     | 1.00        |                        |                          |       |
      | 5        | SPECIAL           |                |                |                    |             |            |                     |             |                        |                          | 0     |
    And the following tariff v2 records exist in db
      | id | name                | description             | equipmentType | pricingType       | status | validFrom  | validTo |
      | 1  | Hourly Bicycle      | Degressive hourly       | BICYCLE       | DEGRESSIVE_HOURLY | ACTIVE | 2026-01-01 |         |
      | 2  | Flat Hourly Scooter | Flat hourly             | SCOOTER       | FLAT_HOURLY       | ACTIVE | 2026-01-01 |         |
      | 3  | Daily Bicycle       | Daily hourly            | BICYCLE       | DAILY             | ACTIVE | 2026-01-01 |         |
      | 4  | Flat Fee Helmet     | Flat fee                | HELMET        | FLAT_FEE          | ACTIVE | 2026-01-01 |         |
      | 5  | Special Tariff      | Apply for any equipment | ANY           | SPECIAL           | ACTIVE | 2025-01-31 |         |
    And the following account records exist in db
      | id   | accountType | customerId |
      | ACC2 | CUSTOMER    | CUS2       |
      | ACC3 | CUSTOMER    | CUS3       |
    And the following sub-ledger records exist in db
      | id     | accountId | ledgerType      | balance | version | createdAt            | updatedAt            |
      | L_C_W2 | ACC2      | CUSTOMER_WALLET | 20.00   | 1       | 2026-03-27T00:00:00Z | 2026-03-27T00:00:00Z |
      | L_C_H2 | ACC2      | CUSTOMER_HOLD   | 16.00   | 1       | 2026-03-27T00:00:00Z | 2026-03-27T00:00:00Z |
      | L_C_W3 | ACC3      | CUSTOMER_WALLET | 10.00   | 1       | 2026-03-27T00:00:00Z | 2026-03-27T00:00:00Z |
      | L_C_H3 | ACC3      | CUSTOMER_HOLD   | 9.00    | 1       | 2026-03-27T00:00:00Z | 2026-03-27T00:00:00Z |

  Scenario Outline: Return equipment - identified by <identified>, no additional payment
    Given now is "<now>"
    And a single rental exists in the database with the following data
      | id         | customerId | status | estimatedCost | plannedDuration | startedAt   | createdAt   | updatedAt   |
      | <rentalId> | CUS2       | ACTIVE | 16.00         | 120             | <startedAt> | <startedAt> | <startedAt> |
    And rental equipments exist in the database with the following data
      | rentalId   | equipmentId   | equipmentUid   | equipmentType | status | startedAt   | expectedReturnAt | estimatedCost | createdAt   | updatedAt   |
      | <rentalId> | <equipmentId> | <equipmentUid> | BICYCLE       | ACTIVE | <startedAt> | <startedAt>      | 16.00         | <startedAt> | <startedAt> |
    And the return equipment request is
      | rentalId   | equipmentIds  | paymentMethod | operatorId |
      | <rentalId> | <equipmentId> | CASH          | <operator> |
    When a POST request has been made to "/api/rentals/return" endpoint
    Then the response status is 200
    And the rental return response contains rental
      | customerId | status    | actualDuration | plannedDuration | estimatedCost | totalCost |
      | CUS2       | COMPLETED | 120            | 120             | 16.00         | 16.00     |
    And the rental return response does contain settlement info
    And the following rental completed event was published
      | rentalId   | equipmentIds  | returnedEquipmentIds | totalCost | returnTime |
      | <rentalId> | <equipmentId> | <equipmentId>        | 16.00     | <now>      |
    And the following equipment record was persisted in db
      | id            | serialNumber | uid      | type    | model   | conditionNotes | condition |
      | <equipmentId> | EQ-001       | BIKE-001 | BICYCLE | Model A | Good           | GOOD      |
    And the following sub-ledger records were persisted in db
      | id      | accountId | ledgerType      | balance |
      | L_C_H2  | ACC2      | CUSTOMER_HOLD   | 0.00    |
      | L_C_W2  | ACC2      | CUSTOMER_WALLET | 20.00   |
      | L_S_REV | ACC_S     | REVENUE         | 16.00   |
    And the following transactions were persisted in db
      | customerId | amount | paymentMethod     | operatorId | type    | recordedAt | sourceId   | sourceType |
      | CUS2       | 16.00  | INTERNAL_TRANSFER | <operator> | CAPTURE | <now>      | <rentalId> | RENTAL     |
    And the following transaction records were persisted in db
      | subLedger | ledgerType    | direction | amount |
      | L_C_H2    | CUSTOMER_HOLD | DEBIT     | 16.00  |
      | L_S_REV   | REVENUE       | CREDIT    | 16.00  |
    Examples:
      | identified   | equipmentUid | rentalId | now                 | startedAt           | operator | equipmentId |
      | rentalId     |              | 10       | 2026-02-10T10:00:00 | 2026-02-10T08:00:00 | OP1      | 1           |
      | equipmentUid | BIKE-001     | 11       | 2026-02-10T10:00:00 | 2026-02-10T08:00:00 | OP1      | 1           |

  Scenario Outline: Return equipment - with overtime, additional payment (5) required - capture from wallet
    Given now is "<now>"
    And a single rental exists in the database with the following data
      | id         | customerId | status | estimatedCost | plannedDuration | startedAt   | createdAt   | updatedAt   |
      | <rentalId> | CUS2       | ACTIVE | 16.00         | 120             | <startedAt> | <startedAt> | <startedAt> |
    And rental equipments exist in the database with the following data
      | rentalId   | equipmentId | equipmentUid | equipmentType | tariffId | status | startedAt   | expectedReturnAt | estimatedCost | createdAt   | updatedAt   |
      | <rentalId> | 1           | BIKE-001     | BICYCLE       | 1        | ACTIVE | <startedAt> | <startedAt>      | 16.00         | <startedAt> | <startedAt> |
    And the return equipment request is
      | rentalId   | paymentMethod | operatorId |
      | <rentalId> | CASH          | OP1        |
    When a POST request has been made to "/api/rentals/return" endpoint
    Then the response status is 200
    And the rental return response contains rental
      | customerId | status    | actualDuration | plannedDuration | estimatedCost | totalCost |
      | CUS2       | COMPLETED | 180            | 120             | 16.00         | 21.00     |
    And the rental return response contains rental equipments
      | equipmentId | equipmentUid | status   | tariffId | estimatedCost | finalCost |
      | 1           | BIKE-001     | RETURNED | 1        | 16.00         | 21.00     |
    And the rental return response does contain settlement info
    And the following rental completed event was published
      | rentalId   | equipmentIds | returnedEquipmentIds | returnTime | totalCost |
      | <rentalId> | 1            | 1                    | <now>      | 21.00     |
    And the following sub-ledger records were persisted in db
      | id      | accountId | ledgerType      | balance |
      | L_C_H2  | ACC2      | CUSTOMER_HOLD   | 0.00    |
      | L_C_W2  | ACC2      | CUSTOMER_WALLET | 15.00   |
      | L_S_REV | ACC_S     | REVENUE         | 21.00   |
    And the following transactions were persisted in db
      | customerId | amount | paymentMethod     | operatorId | type    | recordedAt | sourceId   | sourceType |
      | CUS2       | 16.00  | INTERNAL_TRANSFER | OP1        | CAPTURE | <now>      | <rentalId> | RENTAL     |
      | CUS2       | 5.00   | INTERNAL_TRANSFER | OP1        | CAPTURE | <now>      | <rentalId> | RENTAL     |
    And the following transaction records were persisted in db
      | subLedger | ledgerType      | direction | amount |
      | L_C_H2    | CUSTOMER_HOLD   | DEBIT     | 16.00  |
      | L_S_REV   | REVENUE         | CREDIT    | 16.00  |
      | L_C_W2    | CUSTOMER_WALLET | DEBIT     | 5.00   |
      | L_S_REV   | REVENUE         | CREDIT    | 5.00   |
    Examples:
      | rentalId | now                 | startedAt           |
      | 12       | 2026-02-10T11:30:00 | 2026-02-10T08:30:00 |

  Scenario Outline: Return equipment before expected time - capture from wallet, release hold
    Given now is "<now>"
    And a single rental exists in the database with the following data
      | id         | customerId | status | estimatedCost | plannedDuration | startedAt   | createdAt   | updatedAt   |
      | <rentalId> | CUS2       | ACTIVE | 16.00         | 120             | <startedAt> | <startedAt> | <startedAt> |
    And rental equipments exist in the database with the following data
      | rentalId   | equipmentId | equipmentUid | equipmentType | tariffId | status | startedAt   | expectedReturnAt | estimatedCost | createdAt   | updatedAt   |
      | <rentalId> | 1           | BIKE-001     | BICYCLE       | 1        | ACTIVE | <startedAt> | <startedAt>      | 16.00         | <startedAt> | <startedAt> |
    And the return equipment request is
      | rentalId   | paymentMethod | operatorId |
      | <rentalId> | CASH          | OP1        |
    When a POST request has been made to "/api/rentals/return" endpoint
    Then the response status is 200
    And the rental return response contains rental
      | customerId | status    | actualDuration | plannedDuration | estimatedCost | totalCost |
      | CUS2       | COMPLETED | 60             | 120             | 16.00         | 9.00      |
    And the rental return response contains rental equipments
      | equipmentId | equipmentUid | status   | tariffId | estimatedCost | finalCost |
      | 1           | BIKE-001     | RETURNED | 1        | 16.00         | 9.00      |
    And the rental return response does contain settlement info
    And the following rental completed event was published
      | rentalId   | equipmentIds | returnedEquipmentIds | returnTime | totalCost |
      | <rentalId> | 1            | 1                    | <now>      | 9.00      |
    And the following sub-ledger records were persisted in db
      | id      | accountId | ledgerType      | balance |
      | L_C_H2  | ACC2      | CUSTOMER_HOLD   | 0.00    |
      | L_C_W2  | ACC2      | CUSTOMER_WALLET | 27.00   |
      | L_S_REV | ACC_S     | REVENUE         | 9.00    |
    And the following transactions were persisted in db
      | customerId | amount | paymentMethod     | operatorId | type    | recordedAt | sourceId   | sourceType |
      | CUS2       | 9.00   | INTERNAL_TRANSFER | OP1        | CAPTURE | <now>      | <rentalId> | RENTAL     |
      | CUS2       | 7.00   | INTERNAL_TRANSFER | OP1        | RELEASE | <now>      | <rentalId> | RENTAL     |
    And the following transaction records were persisted in db
      | subLedger | ledgerType      | direction | amount |
      | L_C_H2    | CUSTOMER_HOLD   | DEBIT     | 9.00   |
      | L_S_REV   | REVENUE         | CREDIT    | 9.00   |
      | L_C_W2    | CUSTOMER_WALLET | CREDIT    | 7.00   |
      | L_C_H2    | CUSTOMER_HOLD   | DEBIT     | 7.00   |
    Examples:
      | rentalId | now                 | startedAt           |
      | 12       | 2026-02-10T09:30:00 | 2026-02-10T08:30:00 |

  Scenario Outline: Return equipment - with overtime, additional payment required - DEBT status when wallet balance is insufficient
    Given now is "<now>"
    And a single rental exists in the database with the following data
      | id         | customerId   | status | estimatedCost | plannedDuration | startedAt   | createdAt   | updatedAt   |
      | <rentalId> | <customerId> | ACTIVE | 9.00          | 60              | <startedAt> | <startedAt> | <startedAt> |
    And rental equipments exist in the database with the following data
      | rentalId   | equipmentId | equipmentUid | equipmentType | tariffId | status | startedAt   | expectedReturnAt | estimatedCost | createdAt   | updatedAt   |
      | <rentalId> | 1           | BIKE-001     | BICYCLE       | 1        | ACTIVE | <startedAt> | <startedAt>      | 9.00          | <startedAt> | <startedAt> |
    And the return equipment request is
      | rentalId   | paymentMethod | operatorId |
      | <rentalId> | CASH          | OP1        |
    When a POST request has been made to "/api/rentals/return" endpoint
    Then the response status is 200
    And the rental return response contains rental
      | customerId   | status | actualDuration | plannedDuration | estimatedCost | totalCost |
      | <customerId> | DEBT   | 300            | 60              | 9.00          | 25.00     |
    And the rental return response contains rental equipments
      | equipmentId | equipmentUid | status   | tariffId | estimatedCost | finalCost |
      | 1           | BIKE-001     | RETURNED | 1        | 9.00          | 25.00     |
    And the rental return response does not contain settlement info
    And the following rental completed event was published
      | rentalId   | equipmentIds | returnedEquipmentIds | returnTime | totalCost |
      | <rentalId> | 1            | 1                    | <now>      | 25.00     |
    And the following sub-ledger records were persisted in db
      | id      | accountId | ledgerType      | balance |
      | L_C_H3  | ACC3      | CUSTOMER_HOLD   | 9.00    |
      | L_C_W3  | ACC3      | CUSTOMER_WALLET | 10.00   |
      | L_S_REV | ACC_S     | REVENUE         | 0.00    |
    Examples:
      | rentalId | now                 | startedAt           | customerId |
      | 12       | 2026-02-10T13:30:00 | 2026-02-10T08:30:00 | CUS3       |

  Scenario: Return equipment - rental not found
    And the return equipment request is
      | rentalId | paymentMethod | operatorId |
      | 999      | CASH          | OP1        |
    When a POST request has been made to "/api/rentals/return" endpoint
    Then the response status is 404
    And the response contains
      | path     | value                                  |
      | $.title  | Not Found                              |
      | $.detail | Rental with identifier '999' not found |

  Scenario Outline: Return equipment - rental not in ACTIVE status
    Given a single rental exists in the database with the following data
      | id | customerId | tariffId | status   | estimatedCost | plannedDuration | createdAt           | updatedAt           |
      | 1  | CUS1       | 1        | <status> | 100.00        | 120             | 2026-02-10T10:00:00 | 2026-02-10T10:00:00 |
    And rental equipments exist in the database with the following data
      | rentalId | equipmentId | equipmentUid | equipmentType | tariffId | status   | startedAt           | expectedReturnAt    | estimatedCost | createdAt           | updatedAt           |
      | 1        | 1           | BIKE-001     | BICYCLE       | 1        | ASSIGNED | 2026-02-10T08:00:00 | 2026-02-10T10:00:00 | 100.00        | 2026-02-10T08:00:00 | 2026-02-10T08:00:00 |
    And the return equipment request is
      | rentalId | paymentMethod | operatorId |
      | 1        | CASH          | OP1        |
    When a POST request has been made to "/api/rentals/return" endpoint
    Then the response status is 422
    And the response contains
      | path     | value                                                                            |
      | $.title  | Invalid rental status                                                            |
      | $.detail | Cannot perform operation on rental with status <status>. Expected status: ACTIVE |
    Examples:
      | status    |
      | DEBT      |
      | DRAFT     |
      | CANCELLED |
      | COMPLETED |

  Scenario Outline: Multi-equipment rental. Partial return first, then remaining equipments return
    Given now is "<now>"
    And a single rental exists in the database with the following data
      | id         | customerId   | tariffId | status | estimatedCost | plannedDuration | startedAt   | createdAt   | updatedAt   |
      | <rentalId> | <customerId> | 1        | ACTIVE | 16.00         | 60              | <startedAt> | <startedAt> | <startedAt> |
    And rental equipments exist in the database with the following data
      | rentalId   | equipmentId | equipmentUid | equipmentType | tariffId | status | startedAt   | expectedReturnAt | estimatedCost | createdAt   | updatedAt   |
      | <rentalId> | 1           | BIKE-001     | BICYCLE       | 1        | ACTIVE | <startedAt> | <startedAt>      | 8.00          | <startedAt> | <startedAt> |
      | <rentalId> | 2           | BIKE-002     | BICYCLE       | 1        | ACTIVE | <startedAt> | <startedAt>      | 8.00          | <startedAt> | <startedAt> |
    And the return equipment request is
      | rentalId   | equipmentIds | paymentMethod | operatorId |
      | <rentalId> | <returnedId> | CASH          | <operator> |
    When a POST request has been made to "/api/rentals/return" endpoint
    Then the response status is 200
    And the rental return response contains rental
      | customerId   | status | actualDuration | plannedDuration | estimatedCost | totalCost |
      | <customerId> | ACTIVE | 40             | 60              | 16.00         | 6         |
    And the rental return response contains rental equipments
      | equipmentId   | equipmentUid | status   | tariffId | estimatedCost | finalCost |
      | <returnedId>  | BIKE-001     | RETURNED | 1        | 8.00          | 6         |
      | <remainingId> | BIKE-002     | ACTIVE   | 1        | 8.00          |           |
    And the rental return response does not contain settlement info
    And the following rental completed event was published
      | rentalId   | equipmentIds               | returnedEquipmentIds | returnTime | totalCost |
      | <rentalId> | <returnedId>,<remainingId> | <returnedId>         | <now>      | 6         |
    And the following equipment record was persisted in db
      | id            | serialNumber | uid      | type    | model   | conditionNotes | condition |
      | <returnedId>  | EQ-001       | BIKE-001 | BICYCLE | Model A | Good           | GOOD      |
      | <remainingId> | EQ-002       | BIKE-002 | BICYCLE | Model A | Good           | GOOD      |
#    return the remaining equipment
    Given now is "<nowReturn>"
    And the return equipment request is
      | rentalId   | equipmentIds  | paymentMethod | operatorId |
      | <rentalId> | <remainingId> | CASH          | <operator> |
    When a POST request has been made to "/api/rentals/return" endpoint
    Then the response status is 200
    And the rental return response contains rental
      | customerId   | status    | actualDuration | plannedDuration | estimatedCost | totalCost |
      | <customerId> | COMPLETED | 60             | 60              | 16.00         | 15.00     |
    And the rental return response contains rental equipments
      | equipmentId   | equipmentUid | status   | tariffId | estimatedCost | finalCost |
      | <returnedId>  | BIKE-001     | RETURNED | 1        | 8.00          | 6.00      |
      | <remainingId> | BIKE-002     | RETURNED | 1        | 8.00          | 9.00      |
        #    check finance tables
    And the following sub-ledger records were persisted in db
      | id      | accountId | ledgerType      | balance |
      | L_C_H2  | ACC2      | CUSTOMER_HOLD   | 0.00    |
      | L_C_W2  | ACC2      | CUSTOMER_WALLET | 21.00   |
      | L_S_REV | ACC_S     | REVENUE         | 15.00   |
    And the following transactions were persisted in db
      | customerId   | amount | paymentMethod     | operatorId | type    | recordedAt  | sourceId   | sourceType |
      | <customerId> | 15.00  | INTERNAL_TRANSFER | OP1        | CAPTURE | <nowReturn> | <rentalId> | RENTAL     |
      | <customerId> | 1      | INTERNAL_TRANSFER | OP1        | RELEASE | <nowReturn> | <rentalId> | RENTAL     |
    And the following transaction records were persisted in db
      | subLedger | ledgerType      | direction | amount |
      | L_C_H2    | CUSTOMER_HOLD   | DEBIT     | 15.00  |
      | L_S_REV   | REVENUE         | CREDIT    | 15.00  |
      | L_C_W2    | CUSTOMER_WALLET | CREDIT    | 1.00   |
      | L_C_H2    | CUSTOMER_HOLD   | DEBIT     | 1.00   |
    Examples:
      | rentalId | now                 | nowReturn           | startedAt           | operator | returnedId | remainingId | customerId |
      | 20       | 2026-02-10T08:40:00 | 2026-02-10T09:00:00 | 2026-02-10T08:00:00 | OP1      | 1          | 2           | CUS2       |

  Scenario Outline: Return equipment with discount 10%
    Given now is "<now>"
    And a single rental exists in the database with the following data
      | id         | customerId | status | estimatedCost | plannedDuration | discountPercent | startedAt   | createdAt   | updatedAt   |
      | <rentalId> | CUS2       | ACTIVE | 14.40         | 120             | 10              | <startedAt> | <startedAt> | <startedAt> |
    And rental equipments exist in the database with the following data
      | rentalId   | equipmentId   | equipmentType | status | startedAt   | expectedReturnAt | estimatedCost | createdAt   | updatedAt   |
      | <rentalId> | <equipmentId> | BICYCLE       | ACTIVE | <startedAt> | <startedAt>      | 16.00         | <startedAt> | <startedAt> |
    And the return equipment request is
      | rentalId   | equipmentIds  | paymentMethod | operatorId |
      | <rentalId> | <equipmentId> | CASH          | <operator> |
    When a POST request has been made to "/api/rentals/return" endpoint
    Then the response status is 200
    And the rental return response contains rental
      | customerId | status    | actualDuration | plannedDuration | estimatedCost | totalCost |
      | CUS2       | COMPLETED | 120            | 120             | 14.40         | 14.40     |
    And the rental return response does contain settlement info
    And the following rental completed event was published
      | rentalId   | equipmentIds  | returnedEquipmentIds | totalCost | returnTime |
      | <rentalId> | <equipmentId> | <equipmentId>        | 14.40     | <now>      |
    And the following equipment record was persisted in db
      | id            | serialNumber | uid      | type    | model   | conditionNotes | condition |
      | <equipmentId> | EQ-001       | BIKE-001 | BICYCLE | Model A | Good           | GOOD      |
    And the following sub-ledger records were persisted in db
      | id      | accountId | ledgerType      | balance |
      | L_C_H2  | ACC2      | CUSTOMER_HOLD   | 0.00    |
      | L_C_W2  | ACC2      | CUSTOMER_WALLET | 21.60   |
      | L_S_REV | ACC_S     | REVENUE         | 14.40   |
    And the following transactions were persisted in db
      | customerId | amount | paymentMethod     | operatorId | type    | recordedAt | sourceId   | sourceType |
      | CUS2       | 14.40  | INTERNAL_TRANSFER | <operator> | CAPTURE | <now>      | <rentalId> | RENTAL     |
      | CUS2       | 1.60   | INTERNAL_TRANSFER | <operator> | RELEASE | <now>      | <rentalId> | RENTAL     |
    And the following transaction records were persisted in db
      | subLedger | ledgerType      | direction | amount |
      | L_C_H2    | CUSTOMER_HOLD   | DEBIT     | 1.60   |
      | L_C_W2    | CUSTOMER_WALLET | CREDIT    | 1.60   |
      | L_C_H2    | CUSTOMER_HOLD   | DEBIT     | 14.40  |
      | L_S_REV   | REVENUE         | CREDIT    | 14.40  |
    Examples:
      | rentalId | now                 | startedAt           | operator | equipmentId |
      | 10       | 2026-02-10T10:00:00 | 2026-02-10T08:00:00 | OP1      | 1           |

  Scenario Outline: Final return with SPECIAL pricing — uses flat special price (10 units)
    Given now is "<now>"
    And a single rental exists in the database with the following data
      | id         | customerId | status | specialTariffId | specialPrice | estimatedCost | plannedDuration | startedAt   | createdAt   | updatedAt   |
      | <rentalId> | CUS2       | ACTIVE | 5               | 10.00        | 10.00         | 120             | <startedAt> | <startedAt> | <startedAt> |
    And rental equipments exist in the database with the following data
      | rentalId   | equipmentId   | equipmentType | status | startedAt   | expectedReturnAt | estimatedCost | createdAt   | updatedAt   |
      | <rentalId> | <equipmentId> | BICYCLE       | ACTIVE | <startedAt> | <startedAt>      | 16.00         | <startedAt> | <startedAt> |
    And the return equipment request is
      | rentalId   | equipmentIds  | paymentMethod | operatorId |
      | <rentalId> | <equipmentId> | CASH          | <operator> |
    When a POST request has been made to "/api/rentals/return" endpoint
    Then the response status is 200
    And the rental return response contains rental
      | customerId | status    | actualDuration | plannedDuration | estimatedCost | totalCost |
      | CUS2       | COMPLETED | 120            | 120             | 10.00         | 10.00     |
    And the rental return response does contain settlement info
    And the following rental completed event was published
      | rentalId   | equipmentIds  | returnedEquipmentIds | totalCost | returnTime |
      | <rentalId> | <equipmentId> | <equipmentId>        | 10.00     | <now>      |
    And the following equipment record was persisted in db
      | id            | serialNumber | uid      | type    | model   | conditionNotes | condition |
      | <equipmentId> | EQ-001       | BIKE-001 | BICYCLE | Model A | Good           | GOOD      |
    And the following sub-ledger records were persisted in db
      | id      | accountId | ledgerType      | balance |
      | L_C_H2  | ACC2      | CUSTOMER_HOLD   | 0.00    |
      | L_C_W2  | ACC2      | CUSTOMER_WALLET | 26.00   |
      | L_S_REV | ACC_S     | REVENUE         | 10.00   |
    And the following transactions were persisted in db
      | customerId | amount | paymentMethod     | operatorId | type    | recordedAt | sourceId   | sourceType |
      | CUS2       | 10.00  | INTERNAL_TRANSFER | <operator> | CAPTURE | <now>      | <rentalId> | RENTAL     |
      | CUS2       | 6.00   | INTERNAL_TRANSFER | <operator> | RELEASE | <now>      | <rentalId> | RENTAL     |
    And the following transaction records were persisted in db
      | subLedger | ledgerType      | direction | amount |
      | L_C_H2    | CUSTOMER_HOLD   | DEBIT     | 6.00   |
      | L_C_W2    | CUSTOMER_WALLET | CREDIT    | 6.00   |
      | L_C_H2    | CUSTOMER_HOLD   | DEBIT     | 10.00  |
      | L_S_REV   | REVENUE         | CREDIT    | 10.00  |
    Examples:
      | rentalId | now                 | startedAt           | operator | equipmentId |
      | 10       | 2026-02-10T10:00:00 | 2026-02-10T08:00:00 | OP1      | 1           |

  Scenario Outline: Final return with ZERO SPECIAL pricing
    Given now is "<now>"
    And a single rental exists in the database with the following data
      | id         | customerId | status | specialTariffId | specialPrice | estimatedCost | plannedDuration | startedAt   | createdAt   | updatedAt   |
      | <rentalId> | CUS2       | ACTIVE | 5               | 0.00         | 0.00          | 120             | <startedAt> | <startedAt> | <startedAt> |
    And rental equipments exist in the database with the following data
      | rentalId   | equipmentId   | equipmentType | status | startedAt   | expectedReturnAt | estimatedCost | createdAt   | updatedAt   |
      | <rentalId> | <equipmentId> | BICYCLE       | ACTIVE | <startedAt> | <startedAt>      | 16.00         | <startedAt> | <startedAt> |
    And the return equipment request is
      | rentalId   | equipmentIds  | paymentMethod | operatorId |
      | <rentalId> | <equipmentId> | CASH          | <operator> |
    When a POST request has been made to "/api/rentals/return" endpoint
    Then the response status is 200
    And the rental return response contains rental
      | customerId | status    | actualDuration | plannedDuration | estimatedCost | totalCost |
      | CUS2       | COMPLETED | 120            | 120             | 0.00          | 0.00      |
    And the rental return response does contain settlement info
    And the following rental completed event was published
      | rentalId   | equipmentIds  | returnedEquipmentIds | totalCost | returnTime |
      | <rentalId> | <equipmentId> | <equipmentId>        | 0.00      | <now>      |
    And the following equipment record was persisted in db
      | id            | serialNumber | uid      | type    | model   | conditionNotes | condition |
      | <equipmentId> | EQ-001       | BIKE-001 | BICYCLE | Model A | Good           | GOOD      |
    And the following sub-ledger records were persisted in db
      | id      | accountId | ledgerType      | balance |
      | L_C_H2  | ACC2      | CUSTOMER_HOLD   | 16.00   |
      | L_C_W2  | ACC2      | CUSTOMER_WALLET | 20.00   |
      | L_S_REV | ACC_S     | REVENUE         | 0.00    |
    And there are only 0 transactions in db
    Examples:
      | rentalId | now                 | startedAt           | operator | equipmentId |
      | 10       | 2026-02-10T10:00:00 | 2026-02-10T08:00:00 | OP1      | 1           |