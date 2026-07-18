@ReinitializeSystemLedgers @ResetClock
Feature: Equipment Return
  As an operator
  I want to return rented equipment
  So that I can record partial returns; completing a rental must go through the quote-based return flow

  Background:
    Given the request header "Content-Type" is "application/vnd.bikerental.v1+json"
    And customers exist in the database with the following data
      | id   | phone        | firstName | lastName | email            | birthDate  | comments |
      | CUS1 | +79995551111 | Alex      | Johnson  | null             | null       | null     |
      | CUS2 | +3706861555  | John      | Doe      | john@example.com | 1922-02-22 | null     |
      | CUS3 | +3706861551  | Jane      | Doe      | jane@example.com | 1922-02-21 | null     |
    And the following equipment types exist in the database
      | slug    | name    | description |
      | BICYCLE | Bicycle | Two-wheeled |
    And the following equipment records exist in db
      | id | serialNumber | uid      | type    | model   | conditionNotes | condition |
      | 1  | EQ-001       | BIKE-001 | BICYCLE | Model A | Good           | GOOD      |
      | 2  | EQ-002       | BIKE-002 | BICYCLE | Model A | Good           | GOOD      |
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

  Scenario Outline: Return equipment - identified by <identified> - returning all equipment is rejected
    Given now is "<now>"
    And a single rental exists in the database with the following data
      | id         | customerId | status | estimatedCost | plannedDuration | startedAt   | createdAt   | updatedAt   |
      | <rentalId> | CUS2       | ACTIVE | 16.00         | 120             | <startedAt> | <startedAt> | <startedAt> |
    And rental equipments exist in the database with the following data
      | rentalId   | equipmentId   | equipmentUid   | equipmentType | status | startedAt   | expectedReturnAt   | estimatedCost | createdAt   | updatedAt   |
      | <rentalId> | <equipmentId> | <equipmentUid> | BICYCLE       | ACTIVE | <startedAt> | <expectedReturnAt> | 16.00         | <startedAt> | <startedAt> |
    And the following transaction records exist in db
      | id  | type | paymentMethod | amount | customerId | operatorId | sourceType | sourceId   | recordedAt  | idempotencyKey |
      | TX2 | HOLD | CASH          | 16.00  | CUS2       | OP1        | RENTAL     | <rentalId> | <startedAt> | IDK4           |
    And the return equipment request is
      | rentalId   | equipmentIds  | operatorId |
      | <rentalId> | <equipmentId> | <operator> |
    When a POST request has been made to "/api/rentals/return" endpoint
    Then the response status is 409
    And the response contains
      | path            | value                             |
      | $.errorCode     | rental.completion.flow_violation  |
      | $.params.rentalId | <rentalId>                      |
    Examples:
      | identified   | equipmentUid | rentalId | now                 | startedAt           | expectedReturnAt    | operator | equipmentId |
      | rentalId     |              | 10       | 2026-02-10T10:00:00 | 2026-02-10T08:00:00 | 2026-02-10T10:00:00 | OP1      | 1           |
      | equipmentUid | BIKE-001     | 11       | 2026-02-10T10:00:00 | 2026-02-10T08:00:00 | 2026-02-10T10:00:00 | OP1      | 1           |

  Scenario: Return equipment - rental not found
    And the return equipment request is
      | rentalId | operatorId |
      | 999      | OP1        |
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
      | rentalId | operatorId |
      | 1        | OP1        |
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

  Scenario Outline: Multi-equipment rental. Partial return first, then completing via this endpoint is rejected
    Given now is "<now>"
    And a single rental exists in the database with the following data
      | id         | customerId   | tariffId | status | estimatedCost | plannedDuration | startedAt   | createdAt   | updatedAt   |
      | <rentalId> | <customerId> | 1        | ACTIVE | 16.00         | 60              | <startedAt> | <startedAt> | <startedAt> |
    And rental equipments exist in the database with the following data
      | rentalId   | equipmentId | equipmentUid | equipmentType | tariffId | status | startedAt   | expectedReturnAt   | estimatedCost | createdAt   | updatedAt   |
      | <rentalId> | 1           | BIKE-001     | BICYCLE       | 1        | ACTIVE | <startedAt> | <expectedReturnAt> | 8.00          | <startedAt> | <startedAt> |
      | <rentalId> | 2           | BIKE-002     | BICYCLE       | 1        | ACTIVE | <startedAt> | <expectedReturnAt> | 8.00          | <startedAt> | <startedAt> |
    And the following transaction records exist in db
      | id  | type | paymentMethod | amount | customerId | operatorId | sourceType | sourceId   | recordedAt  | idempotencyKey |
      | TX2 | HOLD | CASH          | 16.00  | CUS2       | OP1        | RENTAL     | <rentalId> | <startedAt> | IDK4           |
    And the return equipment request is
      | rentalId   | equipmentIds | operatorId |
      | <rentalId> | <returnedId> | <operator> |
    When a POST request has been made to "/api/rentals/return" endpoint
    Then the response status is 200
    And the rental return response contains rental
      | customerId   | status | actualDuration | plannedDuration | estimatedCost | totalCost |
      | <customerId> | ACTIVE | 40             | 60              | 16.00         | 6         |
    And the rental return response contains rental equipments
      | equipmentId   | equipmentUid | status   | tariffId | estimatedCost | finalCost | actualReturnAt |
      | <returnedId>  | BIKE-001     | RETURNED | 1        | 8.00          | 6         | <now>          |
      | <remainingId> | BIKE-002     | ACTIVE   | 1        | 8.00          |           |                |
    And the rental return response does not contain settlement info
#    attempting to complete the rental via the legacy endpoint is rejected
    Given now is "<nowReturn>"
    And the return equipment request is
      | rentalId   | equipmentIds  | operatorId |
      | <rentalId> | <remainingId> | <operator> |
    When a POST request has been made to "/api/rentals/return" endpoint
    Then the response status is 409
    And the response contains
      | path              | value                             |
      | $.errorCode       | rental.completion.flow_violation  |
      | $.params.rentalId | <rentalId>                        |
    Examples:
      | rentalId | now                 | nowReturn           | startedAt           | expectedReturnAt    | operator | returnedId | remainingId | customerId |
      | 20       | 2026-02-10T08:40:00 | 2026-02-10T09:00:00 | 2026-02-10T08:00:00 | 2026-02-10T09:00:00 | OP1      | 1          | 2           | CUS2       |

  Scenario: Partial return, then equipment added mid-rental - completing via this endpoint is rejected
    Given now is "2026-02-10T09:00:00"
    And a single rental exists in the database with the following data
      | id | customerId | status | estimatedCost | plannedDuration | startedAt           | expectedReturnAt    | createdAt           | updatedAt           |
      | 50 | CUS2       | ACTIVE | 25.00         | 120             | 2026-02-10T08:00:00 | 2026-02-10T10:00:00 | 2026-02-10T08:00:00 | 2026-02-10T08:00:00 |
    And rental equipments exist in the database with the following data
      | rentalId | equipmentId | equipmentUid | equipmentType | tariffId | status | startedAt           | expectedReturnAt    | estimatedCost | createdAt           | updatedAt           |
      | 50       | 1           | BIKE-001     | BICYCLE       | 1        | ACTIVE | 2026-02-10T08:00:00 | 2026-02-10T10:00:00 | 16.00         | 2026-02-10T08:00:00 | 2026-02-10T08:00:00 |
      | 50       | 2           | BIKE-002     | BICYCLE       | 1        | ACTIVE | 2026-02-10T09:00:00 | 2026-02-10T10:00:00 | 9.00          | 2026-02-10T09:00:00 | 2026-02-10T09:00:00 |
    And the following transaction records exist in db
      | id  | type | paymentMethod | amount | customerId | operatorId | sourceType | sourceId | recordedAt          | idempotencyKey |
      | TX2 | HOLD | CASH          | 16.00  | CUS2       | OP1        | RENTAL     | 50       | 2026-02-10T08:00:00 | IDK4           |
    And the return equipment request is
      | rentalId | equipmentIds | operatorId |
      | 50       | 1            | OP1        |
    When a POST request has been made to "/api/rentals/return" endpoint
    Then the response status is 200
    And the rental return response contains rental
      | customerId | status | actualDuration | plannedDuration | estimatedCost |
      | CUS2       | ACTIVE | 60             | 120             | 25.00         |
    And the rental return response contains rental equipments
      | equipmentId | equipmentUid | status   | startedAt           |
      | 1           | BIKE-001     | RETURNED |                     |
      | 2           | BIKE-002     | ACTIVE   | 2026-02-10T09:00:00 |
    And the rental return response does not contain settlement info
#    attempting to complete the rental via the legacy endpoint is rejected
    Given now is "2026-02-10T10:00:00"
    And the return equipment request is
      | rentalId | equipmentIds | operatorId |
      | 50       | 2            | OP1        |
    When a POST request has been made to "/api/rentals/return" endpoint
    Then the response status is 409
    And the response contains
      | path              | value                            |
      | $.errorCode       | rental.completion.flow_violation |
      | $.params.rentalId | 50                                |
