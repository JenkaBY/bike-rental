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
      | bicycle | Bicycle | Two-wheeled |
    And the following equipment records exist in db
      | id | serialNumber | uid      | status | type    | model   | condition |
      | 1  | EQ-001       | BIKE-001 | RENTED | bicycle | Model A | Good      |
      | 2  | EQ-002       | BIKE-002 | RENTED | bicycle | Model A | Good      |
    And the following tariff records exist in db
      | id | name        | description     | equipmentType | basePrice | halfHourPrice | hourPrice | dayPrice | discountedPrice | validFrom  | validTo    | status |
      | 1  | Hourly Rate | Standard hourly | bicycle       | 100.00    | 60.00         | 100.00    | 500.00   | 90.00           | 2026-01-01 | 2026-12-31 | ACTIVE |
    And the following account records exist in db
      | id   | accountType | customerId |
      | ACC2 | CUSTOMER    | CUS2       |
      | ACC3 | CUSTOMER    | CUS3       |
    And the following sub-ledger records exist in db
      | id     | accountId | ledgerType      | balance | version | createdAt            | updatedAt            |
      | L_C_W2 | ACC2      | CUSTOMER_WALLET | 100.00  | 1       | 2026-03-27T00:00:00Z | 2026-03-27T00:00:00Z |
      | L_C_H2 | ACC2      | CUSTOMER_HOLD   | 200.00  | 1       | 2026-03-27T00:00:00Z | 2026-03-27T00:00:00Z |
      | L_C_W3 | ACC3      | CUSTOMER_WALLET | 100.00  | 1       | 2026-03-27T00:00:00Z | 2026-03-27T00:00:00Z |
      | L_C_H3 | ACC3      | CUSTOMER_HOLD   | 400.00  | 1       | 2026-03-27T00:00:00Z | 2026-03-27T00:00:00Z |

  Scenario Outline: Return equipment - identified by <identified>, no additional payment
    Given now is "<now>"
    And a single rental exists in the database with the following data
      | id         | customerId | status | estimatedCost | plannedDuration | startedAt   | createdAt   | updatedAt   |
      | <rentalId> | CUS2       | ACTIVE | 200.00        | 120             | <startedAt> | <startedAt> | <startedAt> |
    And rental equipments exist in the database with the following data
      | rentalId   | equipmentId   | equipmentUid   | equipmentType | tariffId | status | startedAt   | expectedReturnAt | estimatedCost | createdAt   | updatedAt   |
      | <rentalId> | <equipmentId> | <equipmentUid> | bicycle       | 1        | ACTIVE | <startedAt> | <startedAt>      | 200.00        | <startedAt> | <startedAt> |
    And the return equipment request is
      | rentalId   | equipmentIds  | paymentMethod | operatorId |
      | <rentalId> | <equipmentId> | CASH          | <operator> |
    When a POST request has been made to "/api/rentals/return" endpoint
    Then the response status is 200
    And the rental return response contains rental
      | customerId | status    | actualDuration | plannedDuration | estimatedCost | totalCost |
      | CUS2       | COMPLETED | 120            | 120             | 200.00        | 200.00    |
    And the rental return response contains the following break down costs
      | equipmentId   | baseCost | overtimeCost | finalCost | actualMinutes | billableMinutes | plannedMinutes | overtimeMinutes | forgivenessApplied | calculationMessage |
      | <equipmentId> | 200.00   | 0.00         | 200.00    | 120           | 120             | 120            | 0               | true               |                    |
    And the rental return response does contain settlement info
    And the following rental completed event was published
      | rentalId   | equipmentIds  | returnedEquipmentIds | totalCost | returnTime |
      | <rentalId> | <equipmentId> | <equipmentId>        | 200.00    | <now>      |
    And the following equipment record was persisted in db
      | id            | serialNumber | uid      | status    | type    | model   | condition |
      | <equipmentId> | EQ-001       | BIKE-001 | AVAILABLE | bicycle | Model A | Good      |
#    check finance tables
    And the following sub-ledger records were persisted in db
      | id      | accountId | ledgerType      | balance |
      | L_C_H2  | ACC2      | CUSTOMER_HOLD   | 0.00    |
      | L_C_W2  | ACC2      | CUSTOMER_WALLET | 100.00  |
      | L_S_REV | ACC_S     | REVENUE         | 200.00  |
    And the following transactions were persisted in db
      | customerId | amount | paymentMethod     | operatorId | type    | recordedAt | sourceId   | sourceType |
      | CUS2       | 200.00 | INTERNAL_TRANSFER | <operator> | CAPTURE | <now>      | <rentalId> | RENTAL     |
    And the following transaction records were persisted in db
      | subLedger | ledgerType    | direction | amount |
      | L_C_H2    | CUSTOMER_HOLD | DEBIT     | 200.00 |
      | L_S_REV   | REVENUE       | CREDIT    | 200.00 |
    Examples:
      | identified   | equipmentUid | rentalId | now                 | startedAt           | operator | equipmentId |
      | rentalId     |              | 10       | 2026-02-10T10:00:00 | 2026-02-10T08:00:00 | OP1      | 1           |
      | equipmentUid | BIKE-001     | 11       | 2026-02-10T10:00:00 | 2026-02-10T08:00:00 | OP1      | 1           |

  Scenario Outline: Return equipment - with overtime, additional payment required - capture from wallet
    Given now is "<now>"
    And a single rental exists in the database with the following data
      | id         | customerId | status | estimatedCost | plannedDuration | startedAt   | createdAt   | updatedAt   |
      | <rentalId> | CUS2       | ACTIVE | 200.00        | 120             | <startedAt> | <startedAt> | <startedAt> |
    And rental equipments exist in the database with the following data
      | rentalId   | equipmentId | equipmentUid | equipmentType | tariffId | status | startedAt   | expectedReturnAt | estimatedCost | createdAt   | updatedAt   |
      | <rentalId> | 1           | BIKE-001     | bicycle       | 1        | ACTIVE | <startedAt> | <startedAt>      | 200.00        | <startedAt> | <startedAt> |
    And the return equipment request is
      | rentalId   | paymentMethod | operatorId |
      | <rentalId> | CASH          | OP1        |
    When a POST request has been made to "/api/rentals/return" endpoint
    Then the response status is 200
    And the rental return response contains rental
      | customerId | status    | actualDuration | plannedDuration | estimatedCost | totalCost |
      | CUS2       | COMPLETED | 180            | 120             | 200.00        | 300.00    |
    And the rental return response contains rental equipments
      | equipmentId | equipmentUid | status   | tariffId | estimatedCost | finalCost |
      | 1           | BIKE-001     | RETURNED | 1        | 200.00        | 300.00    |
    And the rental return response contains the following break down costs
      | equipmentId | baseCost | overtimeCost | finalCost | actualMinutes | billableMinutes | plannedMinutes | overtimeMinutes | forgivenessApplied |
      | 1           | 300.00   | 0            | 300.00    | 180           | 180             | 120            | 60              | true               |
    And the rental return response does contain settlement info
    And the following rental completed event was published
      | rentalId   | equipmentIds | returnedEquipmentIds | returnTime | totalCost |
      | <rentalId> | 1            | 1                    | <now>      | 300.00    |
    #    check finance tables
    And the following sub-ledger records were persisted in db
      | id      | accountId | ledgerType      | balance |
      | L_C_H2  | ACC2      | CUSTOMER_HOLD   | 0.00    |
      | L_C_W2  | ACC2      | CUSTOMER_WALLET | 0.00    |
      | L_S_REV | ACC_S     | REVENUE         | 300.00  |
    And the following transactions were persisted in db
      | customerId | amount | paymentMethod     | operatorId | type    | recordedAt | sourceId   | sourceType |
      | CUS2       | 200.00 | INTERNAL_TRANSFER | OP1        | CAPTURE | <now>      | <rentalId> | RENTAL     |
      | CUS2       | 100.00 | INTERNAL_TRANSFER | OP1        | CAPTURE | <now>      | <rentalId> | RENTAL     |
    And the following transaction records were persisted in db
      | subLedger | ledgerType      | direction | amount |
      | L_C_H2    | CUSTOMER_HOLD   | DEBIT     | 200.00 |
      | L_S_REV   | REVENUE         | CREDIT    | 200.00 |
      | L_C_W2    | CUSTOMER_WALLET | DEBIT     | 100.00 |
      | L_S_REV   | REVENUE         | CREDIT    | 100.00 |
    Examples:
      | rentalId | now                 | startedAt           |
      | 12       | 2026-02-10T11:30:00 | 2026-02-10T08:30:00 |

  Scenario Outline: Return equipment - capture from wallet, release hold
    Given now is "<now>"
    And a single rental exists in the database with the following data
      | id         | customerId | status | estimatedCost | plannedDuration | startedAt   | createdAt   | updatedAt   |
      | <rentalId> | CUS2       | ACTIVE | 200.00        | 120             | <startedAt> | <startedAt> | <startedAt> |
    And rental equipments exist in the database with the following data
      | rentalId   | equipmentId | equipmentUid | equipmentType | tariffId | status | startedAt   | expectedReturnAt | estimatedCost | createdAt   | updatedAt   |
      | <rentalId> | 1           | BIKE-001     | bicycle       | 1        | ACTIVE | <startedAt> | <startedAt>      | 200.00        | <startedAt> | <startedAt> |
    And the return equipment request is
      | rentalId   | paymentMethod | operatorId |
      | <rentalId> | CASH          | OP1        |
    When a POST request has been made to "/api/rentals/return" endpoint
    Then the response status is 200
    And the rental return response contains rental
      | customerId | status    | actualDuration | plannedDuration | estimatedCost | totalCost |
      | CUS2       | COMPLETED | 60             | 120             | 200.00        | 100.00    |
    And the rental return response contains rental equipments
      | equipmentId | equipmentUid | status   | tariffId | estimatedCost | finalCost |
      | 1           | BIKE-001     | RETURNED | 1        | 200.00        | 100.00    |
    And the rental return response contains the following break down costs
      | equipmentId | baseCost | overtimeCost | finalCost | actualMinutes | billableMinutes | plannedMinutes | overtimeMinutes | forgivenessApplied |
      | 1           | 100.00   | 0            | 100.00    | 60            | 60              | 120            | -60             | true               |
    And the rental return response does contain settlement info
    And the following rental completed event was published
      | rentalId   | equipmentIds | returnedEquipmentIds | returnTime | totalCost |
      | <rentalId> | 1            | 1                    | <now>      | 100.00    |
    #    check finance tables
    And the following sub-ledger records were persisted in db
      | id      | accountId | ledgerType      | balance |
      | L_C_H2  | ACC2      | CUSTOMER_HOLD   | 0.00    |
      | L_C_W2  | ACC2      | CUSTOMER_WALLET | 200.00  |
      | L_S_REV | ACC_S     | REVENUE         | 100.00  |
    And the following transactions were persisted in db
      | customerId | amount | paymentMethod     | operatorId | type    | recordedAt | sourceId   | sourceType |
      | CUS2       | 100.00 | INTERNAL_TRANSFER | OP1        | CAPTURE | <now>      | <rentalId> | RENTAL     |
      | CUS2       | 100.00 | INTERNAL_TRANSFER | OP1        | RELEASE | <now>      | <rentalId> | RENTAL     |
    And the following transaction records were persisted in db
      | subLedger | ledgerType      | direction | amount |
      | L_C_H2    | CUSTOMER_HOLD   | DEBIT     | 100.00 |
      | L_S_REV   | REVENUE         | CREDIT    | 100.00 |
      | L_C_W2    | CUSTOMER_WALLET | CREDIT    | 100.00 |
      | L_C_H2    | CUSTOMER_HOLD   | DEBIT     | 100.00 |
    Examples:
      | rentalId | now                 | startedAt           |
      | 12       | 2026-02-10T09:30:00 | 2026-02-10T08:30:00 |

  Scenario Outline: Return equipment - with overtime, additional payment required - DEBT status when wallet balance is insufficient
    Given now is "<now>"
    And a single rental exists in the database with the following data
      | id         | customerId | status | estimatedCost | plannedDuration | startedAt   | createdAt   | updatedAt   |
      | <rentalId> | CUS2       | ACTIVE | 200.00        | 120             | <startedAt> | <startedAt> | <startedAt> |
    And rental equipments exist in the database with the following data
      | rentalId   | equipmentId | equipmentUid | equipmentType | tariffId | status | startedAt   | expectedReturnAt | estimatedCost | createdAt   | updatedAt   |
      | <rentalId> | 1           | BIKE-001     | bicycle       | 1        | ACTIVE | <startedAt> | <startedAt>      | 200.00        | <startedAt> | <startedAt> |
    And the return equipment request is
      | rentalId   | paymentMethod | operatorId |
      | <rentalId> | CASH          | OP1        |
    When a POST request has been made to "/api/rentals/return" endpoint
    Then the response status is 200
    And the rental return response contains rental
      | customerId | status | actualDuration | plannedDuration | estimatedCost | totalCost |
      | CUS2       | DEBT   | 190            | 120             | 200.00        | 400.00    |
    And the rental return response contains rental equipments
      | equipmentId | equipmentUid | status   | tariffId | estimatedCost | finalCost |
      | 1           | BIKE-001     | RETURNED | 1        | 200.00        | 400.00    |
    And the rental return response contains the following break down costs
      | equipmentId | baseCost | overtimeCost | finalCost | actualMinutes | billableMinutes | plannedMinutes | overtimeMinutes | forgivenessApplied |
      | 1           | 400.00   | 0            | 400.00    | 190           | 190             | 120            | 70              | true               |
    And the rental return response does not contain settlement info
    And the following rental completed event was published
      | rentalId   | equipmentIds | returnedEquipmentIds | returnTime | totalCost |
      | <rentalId> | 1            | 1                    | <now>      | 400.00    |
    #    check finance tables
    And the following sub-ledger records were persisted in db
      | id      | accountId | ledgerType      | balance |
      | L_C_H2  | ACC2      | CUSTOMER_HOLD   | 200.00  |
      | L_C_W2  | ACC2      | CUSTOMER_WALLET | 100.00  |
      | L_S_REV | ACC_S     | REVENUE         | 0.00    |
    Examples:
      | rentalId | now                 | startedAt           |
      | 12       | 2026-02-10T11:40:00 | 2026-02-10T08:30:00 |

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
      | 1        | 1           | BIKE-001     | bicycle       | 1        | ASSIGNED | 2026-02-10T08:00:00 | 2026-02-10T10:00:00 | 100.00        | 2026-02-10T08:00:00 | 2026-02-10T08:00:00 |
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
      | id         | customerId | tariffId | status | estimatedCost | plannedDuration | startedAt   | createdAt   | updatedAt   |
      | <rentalId> | CUS3       | 1        | ACTIVE | 400.00        | 120             | <startedAt> | <startedAt> | <startedAt> |
    And rental equipments exist in the database with the following data
      | rentalId   | equipmentId | equipmentUid | equipmentType | tariffId | status | startedAt   | expectedReturnAt | estimatedCost | createdAt   | updatedAt   |
      | <rentalId> | 1           | BIKE-001     | bicycle       | 1        | ACTIVE | <startedAt> | <startedAt>      | 200.00        | <startedAt> | <startedAt> |
      | <rentalId> | 2           | BIKE-002     | bicycle       | 1        | ACTIVE | <startedAt> | <startedAt>      | 200.00        | <startedAt> | <startedAt> |
    And the return equipment request is
      | rentalId   | equipmentIds | paymentMethod | operatorId |
      | <rentalId> | <returnedId> | CASH          | <operator> |
    When a POST request has been made to "/api/rentals/return" endpoint
    Then the response status is 200
    And the rental return response contains rental
      | customerId | status | actualDuration | plannedDuration | estimatedCost | totalCost |
      | CUS3       | ACTIVE | 60             | 120             | 400.00        | 100.00    |
    And the rental return response contains rental equipments
      | equipmentId   | equipmentUid | status   | tariffId | estimatedCost | finalCost |
      | <returnedId>  | BIKE-001     | RETURNED | 1        | 200.00        | 100.00    |
      | <remainingId> | BIKE-002     | ACTIVE   | 1        | 200.00        |           |
    And the rental return response contains the following break down costs
      | equipmentId  | tariffId | baseCost | overtimeCost | finalCost | actualMinutes | billableMinutes | plannedMinutes | overtimeMinutes | forgivenessApplied |
      | <returnedId> | 1        | 100.00   | 0            | 100.00    | 60            | 60              | 120            | -60             | true               |
    And the rental return response does not contain settlement info
    And the following rental completed event was published
      | rentalId   | equipmentIds               | returnedEquipmentIds | returnTime | totalCost |
      | <rentalId> | <returnedId>,<remainingId> | <returnedId>         | <now>      | 100       |
    And the following equipment record was persisted in db
      | id            | serialNumber | uid      | status    | type    | model   | condition |
      | <returnedId>  | EQ-001       | BIKE-001 | AVAILABLE | bicycle | Model A | Good      |
      | <remainingId> | EQ-002       | BIKE-002 | RENTED    | bicycle | Model A | Good      |
#    return the remaining equipment
    Given now is "<nowReturn>"
    And the return equipment request is
      | rentalId   | equipmentIds  | paymentMethod | operatorId |
      | <rentalId> | <remainingId> | CASH          | <operator> |
    When a POST request has been made to "/api/rentals/return" endpoint
    Then the response status is 200
    And the rental return response contains rental
      | customerId | status    | actualDuration | plannedDuration | estimatedCost | totalCost |
      | CUS3       | COMPLETED | 120            | 120             | 400.00        | 300.00    |
    And the rental return response contains the following break down costs
      | equipmentId   | baseCost | overtimeCost | finalCost | actualMinutes | billableMinutes | plannedMinutes | overtimeMinutes | forgivenessApplied |
      | <remainingId> | 200.00   | 0.00         | 200.00    | 120           | 120             | 120            | 0               | true               |
    And the rental return response contains rental equipments
      | equipmentId   | equipmentUid | status   | tariffId | estimatedCost | finalCost |
      | <returnedId>  | BIKE-001     | RETURNED | 1        | 200.00        | 100.00    |
      | <remainingId> | BIKE-002     | RETURNED | 1        | 200.00        | 200.00    |
        #    check finance tables
    And the following sub-ledger records were persisted in db
      | id      | accountId | ledgerType      | balance |
      | L_C_H3  | ACC3      | CUSTOMER_HOLD   | 0.00    |
      | L_C_W3  | ACC3      | CUSTOMER_WALLET | 200.00  |
      | L_S_REV | ACC_S     | REVENUE         | 300.00  |
    And the following transactions were persisted in db
      | customerId | amount | paymentMethod     | operatorId | type    | recordedAt  | sourceId   | sourceType |
      | CUS3       | 300.00 | INTERNAL_TRANSFER | OP1        | CAPTURE | <nowReturn> | <rentalId> | RENTAL     |
      | CUS3       | 100.00 | INTERNAL_TRANSFER | OP1        | RELEASE | <nowReturn> | <rentalId> | RENTAL     |
    And the following transaction records were persisted in db
      | subLedger | ledgerType      | direction | amount |
      | L_C_H3    | CUSTOMER_HOLD   | DEBIT     | 300.00 |
      | L_S_REV   | REVENUE         | CREDIT    | 300.00 |
      | L_C_W3    | CUSTOMER_WALLET | CREDIT    | 100.00 |
      | L_C_H3    | CUSTOMER_HOLD   | DEBIT     | 100.00 |
    Examples:
      | rentalId | now                 | nowReturn           | startedAt           | operator | returnedId | remainingId |
      | 20       | 2026-02-10T09:00:00 | 2026-02-10T10:00:00 | 2026-02-10T08:00:00 | OP1      | 1          | 2           |