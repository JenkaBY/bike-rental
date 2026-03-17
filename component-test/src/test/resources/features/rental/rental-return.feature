Feature: Equipment Return
  As an operator
  I want to return rented equipment
  So that I can complete a rental and track equipment availability

  Background:
    Given the request header "Content-Type" is "application/vnd.bikerental.v1+json"
    And customers exist in the database with the following data
      | id   | phone        | firstName | lastName | email | birthDate | comments |
      | CUS1 | +79995551111 | Alex      | Johnson  | null  | null      | null     |
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

  Scenario Outline: Return equipment - identified by <identified>, no additional payment
    Given now is "<now>"
    And a single rental exists in the database with the following data
      | id         | customerId | status | estimatedCost | plannedDuration | startedAt   | createdAt   | updatedAt   |
      | <rentalId> | CUS1       | ACTIVE | 200.00        | 120             | <startedAt> | <startedAt> | <startedAt> |
    And rental equipments exist in the database with the following data
      | rentalId   | equipmentId   | equipmentUid   | tariffId | status | startedAt   | expectedReturnAt | estimatedCost | createdAt   | updatedAt   |
      | <rentalId> | <equipmentId> | <equipmentUid> | 1        | ACTIVE | <startedAt> | <startedAt>      | 200.00        | <startedAt> | <startedAt> |
    And the following payment record exists in db
      | id   | rentalId   | amount | type       | method | operator | receipt   |
      | PAY1 | <rentalId> | 200.00 | PREPAYMENT | CASH   | OP1      | <receipt> |
    And the return equipment request is
      | rentalId   | equipmentIds  | paymentMethod | operatorId |
      | <rentalId> | <equipmentId> | CASH          | <operator> |
    When a POST request has been made to "/api/rentals/return" endpoint
    Then the response status is 200
    And the rental return response contains
      | additionalPayment | paymentMethod | receiptNumber | paymentAmount |
      | 0                 |               |               |               |
    And the rental return response contains the following break down costs
      | equipmentId   | baseCost | overtimeCost | totalCost | actualMinutes | billableMinutes | plannedMinutes | overtimeMinutes | forgivenessApplied | calculationMessage |
      | <equipmentId> | 200.00   | 0.00         | 200.00    | 120           | 120             | 120            | 0               | true               |                    |
    And the following rental completed event was published
      | rentalId   | equipmentIds  | returnedEquipmentIds | totalCost | returnTime |
      | <rentalId> | <equipmentId> | <equipmentId>        | 200.00    | <now>      |
    And the following equipment record was persisted in db
      | id            | serialNumber | uid      | status    | type    | model   | condition |
      | <equipmentId> | EQ-001       | BIKE-001 | AVAILABLE | bicycle | Model A | Good      |
    Examples:
      | identified   | equipmentUid | rentalId | now                 | startedAt           | operator | receipt | equipmentId |
      | rentalId     |              | 10       | 2026-02-10T10:00:00 | 2026-02-10T08:00:00 | OP1      | REC-10  | 1           |
      | equipmentUid | BIKE-001     | 11       | 2026-02-10T10:00:00 | 2026-02-10T08:00:00 | OP1      | REC-11  | 1           |

  Scenario Outline: Return equipment - with overtime, additional payment required
    Given now is "<now>"
    And a single rental exists in the database with the following data
      | id         | customerId | tariffId | status | estimatedCost | plannedDuration | startedAt   | createdAt   | updatedAt   |
      | <rentalId> | CUS1       | 1        | ACTIVE | 100.00        | 30              | <startedAt> | <startedAt> | <startedAt> |
    And rental equipments exist in the database with the following data
      | rentalId   | equipmentId | equipmentUid | tariffId | status | startedAt   | expectedReturnAt | estimatedCost | createdAt   | updatedAt   |
      | <rentalId> | 1           | BIKE-001     | 1        | ACTIVE | <startedAt> | <startedAt>      | 100.00        | <startedAt> | <startedAt> |
    And the following payment record exists in db
      | id   | rentalId   | amount | type       | method | operator | receipt |
      | PAY1 | <rentalId> | 100.00 | PREPAYMENT | CASH   | OP1      | REC-12  |
    And the return equipment request is
      | rentalId   | paymentMethod | operatorId |
      | <rentalId> | CASH          | OP1        |
    When a POST request has been made to "/api/rentals/return" endpoint
    Then the response status is 200
    And the rental return response contains
      | additionalPayment | paymentMethod | paymentAmount |
      | 100.00            | CASH          | 100.00        |
    And the rental return response contains rental equipments
      | equipmentId | equipmentUid | status   | tariffId | estimatedCost | finalCost |
      | 1           | BIKE-001     | RETURNED | 1        | 100.00        | 200.00    |
    And the rental return response contains the following break down costs
      | equipmentId | baseCost | overtimeCost | totalCost | actualMinutes | billableMinutes | plannedMinutes | overtimeMinutes | forgivenessApplied |
      | 1           | 200.00   | 0.00         | 200.00    | 90            | 90              | 30             | 60              | true               |
    And the following rental completed event was published
      | rentalId   | equipmentIds | returnedEquipmentIds | returnTime | totalCost |
      | <rentalId> | 1            | 1                    | <now>      | 200.00    |
    Examples:
      | rentalId | now                 | startedAt           |
      | 12       | 2026-02-10T10:00:00 | 2026-02-10T08:30:00 |

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
      | rentalId | equipmentId | equipmentUid | tariffId | status   | startedAt           | expectedReturnAt    | estimatedCost | createdAt           | updatedAt           |
      | 1        | 1           | BIKE-001     | 1        | ASSIGNED | 2026-02-10T08:00:00 | 2026-02-10T10:00:00 | 100.00        | 2026-02-10T08:00:00 | 2026-02-10T08:00:00 |
    And the return equipment request is
      | rentalId | paymentMethod | operatorId |
      | 1        | CASH          | Op1        |
    When a POST request has been made to "/api/rentals/return" endpoint
    Then the response status is 422
    And the response contains
      | path     | value                                                                            |
      | $.title  | Invalid rental status                                                            |
      | $.detail | Cannot perform operation on rental with status <status>. Expected status: ACTIVE |
    Examples:
      | status    |
      | DRAFT     |
      | CANCELLED |
      | COMPLETED |

  @ResetClock
  Scenario Outline: Partial return - return one equipment from multi-equipment rental
    Given now is "<now>"
    And a single rental exists in the database with the following data
      | id         | customerId | tariffId | status | estimatedCost | plannedDuration | startedAt   | createdAt   | updatedAt   |
      | <rentalId> | CUS1       | 1        | ACTIVE | 400.00        | 120             | <startedAt> | <startedAt> | <startedAt> |
    And rental equipments exist in the database with the following data
      | rentalId   | equipmentId | equipmentUid | tariffId | status | startedAt   | expectedReturnAt | estimatedCost | createdAt   | updatedAt   |
      | <rentalId> | 1           | BIKE-001     | 1        | ACTIVE | <startedAt> | <startedAt>      | 200.00        | <startedAt> | <startedAt> |
      | <rentalId> | 2           | BIKE-002     | 1        | ACTIVE | <startedAt> | <startedAt>      | 200.00        | <startedAt> | <startedAt> |
    And the following payment records exist in db
      | id   | rentalId   | amount | type       | method | operator | receipt |
      | PAY1 | <rentalId> | 400.00 | PREPAYMENT | CASH   | OP1      | REC-P1  |
    And the return equipment request is
      | rentalId   | equipmentIds | paymentMethod | operatorId |
      | <rentalId> | <returnedId> | CASH          | <operator> |
    When a POST request has been made to "/api/rentals/return" endpoint
    Then the response status is 200
    And the rental return response contains
      | additionalPayment | paymentMethod | receiptNumber | paymentAmount |
      | 0.00              |               |               |               |
    And the rental return response contains rental
      | customerId | status | actualDuration | plannedDuration | estimatedCost |
      | CUS1       | ACTIVE | 120            | 120             | 400.00        |
    And the rental return response contains rental equipments
      | equipmentId   | equipmentUid | status   | tariffId | estimatedCost | finalCost |
      | <returnedId>  | BIKE-001     | RETURNED | 1        | 200.00        | 200.00    |
      | <remainingId> | BIKE-002     | ACTIVE   | 1        | 200.00        |           |
    And the rental return response contains the following break down costs
      | equipmentId  | baseCost | overtimeCost | totalCost | actualMinutes | billableMinutes | plannedMinutes | overtimeMinutes | forgivenessApplied | calculationMessage |
      | <returnedId> | 200.00   | 0.00         | 200.00    | 120           | 120             | 120            | 0               | true               |                    |
    And the following rental completed event was published
      | rentalId   | equipmentIds               | returnedEquipmentIds | returnTime | totalCost |
      | <rentalId> | <returnedId>,<remainingId> | <returnedId>         | <now>      | 200.00    |
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
#    FIXME wrong calculation logic
    And the rental return response contains
      | additionalPayment | paymentMethod | receiptNumber | paymentAmount |
      | 100.00            |               |               |               |
    And the rental return response contains rental
      | customerId | status    | actualDuration | plannedDuration | estimatedCost | finalCost |
      | CUS1       | COMPLETED | 180            | 120             | 400.00        | 500.00    |
    And the rental return response contains the following break down costs
      | equipmentId   | baseCost | overtimeCost | totalCost | actualMinutes | billableMinutes | plannedMinutes | overtimeMinutes | forgivenessApplied | calculationMessage |
      | <remainingId> | 300.00   | 0.00         | 300.00    | 180           | 180             | 120            | 60              | true               |                    |
    And the rental return response contains rental equipments
      | equipmentId   | equipmentUid | status   | tariffId | estimatedCost | finalCost |
      | <returnedId>  | BIKE-001     | RETURNED | 1        | 200.00        | 200.00    |
      | <remainingId> | BIKE-002     | RETURNED | 1        | 200.00        | 300.00    |
    Examples:
      | rentalId | now                 | nowReturn           | startedAt           | operator | returnedId | remainingId |
      | 20       | 2026-02-10T10:00:00 | 2026-02-10T11:00:00 | 2026-02-10T08:00:00 | OP1      | 1          | 2           |