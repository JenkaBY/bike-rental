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
      | status    | baseCost | overtimeCost | finalCost | actualMinutes | plannedMinutes | overtimeMinutes | forgivenessApplied | additionalPayment |
      | COMPLETED | 200.00   | 0.00         | 200.00    | 120           | 120            | 0               | true               | 0.00              |
    And the following rental completed event was published
      | rentalId   | equipmentId   | finalCost | returnTime |
      | <rentalId> | <equipmentId> | 200.00    | <now>      |
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
      | status    | baseCost | overtimeMinutes | plannedMinutes | actualMinutes | forgivenessApplied | additionalPayment |
      | COMPLETED | 200.00   | 60              | 30             | 90            | true               | 100.00            |
    And the following rental completed event was published
      | rentalId   | equipmentId | returnTime | finalCost |
      | <rentalId> | 1           | <now>      | 200.00    |
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