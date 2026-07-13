@ReinitializeSystemLedgers @ResetClock
Feature: Equipment Return — Confirm via Cost Quote
  As an operator
  I want to confirm a rental return against a previously created cost quote
  So that the amount I saw at preview is exactly the amount charged, regardless of how much time passes before I confirm

  Background:
    Given the request header "Content-Type" is "application/vnd.bikerental.v1+json"
    And customers exist in the database with the following data
      | id   | phone       | firstName | lastName | email            | birthDate  | comments |
      | CUS2 | +3706861555 | John      | Doe      | john@example.com | 1922-02-22 | null     |
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
    And the pricing params list for tariff request is
      | tariffId | pricingType       | firstHourPrice | hourlyDiscount | minimumHourlyPrice | minimumDurationMinutes | minimumDurationSurcharge | price |
      | 1        | DEGRESSIVE_HOURLY | 9.00           | 2.00           | 1.00               | 30                     | 1.00                     |       |
      | 5        | SPECIAL           |                |                |                    |                        |                          | 0     |
    And the following tariff v2 records exist in db
      | id | name           | description             | equipmentType | pricingType       | status | validFrom  | validTo |
      | 1  | Hourly Bicycle | Degressive hourly       | BICYCLE       | DEGRESSIVE_HOURLY | ACTIVE | 2026-01-01 |         |
      | 5  | Special Tariff | Apply for any equipment | ANY           | SPECIAL           | ACTIVE | 2025-01-31 |         |
    And the following account records exist in db
      | id   | accountType | customerId |
      | ACC2 | CUSTOMER    | CUS2       |
    And the following sub-ledger records exist in db
      | id     | accountId | ledgerType      | balance | version | createdAt            | updatedAt            |
      | L_C_W2 | ACC2      | CUSTOMER_WALLET | 20.00   | 1       | 2026-03-27T00:00:00Z | 2026-03-27T00:00:00Z |
      | L_C_H2 | ACC2      | CUSTOMER_HOLD   | 16.00   | 1       | 2026-03-27T00:00:00Z | 2026-03-27T00:00:00Z |

  Scenario: Confirm charges exactly the frozen quote total
    Given now is "2026-02-10T11:30:00"
    And a single rental exists in the database with the following data
      | id  | customerId | status | estimatedCost | plannedDuration | startedAt           | createdAt           | updatedAt           |
      | 100 | CUS2       | ACTIVE | 16.00         | 120             | 2026-02-10T08:30:00 | 2026-02-10T08:30:00 | 2026-02-10T08:30:00 |
    And rental equipments exist in the database with the following data
      | rentalId | equipmentId | equipmentUid | equipmentType | tariffId | status | startedAt           | expectedReturnAt    | estimatedCost | createdAt           | updatedAt           |
      | 100      | 1           | BIKE-001     | BICYCLE       | 1        | ACTIVE | 2026-02-10T08:30:00 | 2026-02-10T10:30:00 | 16.00         | 2026-02-10T08:30:00 | 2026-02-10T08:30:00 |
    And the following transaction records exist in db
      | id  | type | paymentMethod | amount | customerId | operatorId | sourceType | sourceId | recordedAt          | idempotencyKey |
      | TX2 | HOLD | CASH          | 16.00  | CUS2       | OP1        | RENTAL     | 100      | 2026-02-10T08:30:00 | IDK4           |
    And the equipment items for cost calculation request are
      | equipmentId | equipmentType | returnAt            |
      | 1           | BICYCLE       | 2026-02-10T11:30:00 |
    And the rental cost calculation request is prepared with the following data
      | startAt             | plannedDurationMinutes |
      | 2026-02-10T08:30:00 | 120                    |
    When a POST request has been made to "/api/tariffs/quotes" endpoint
    Then the response status is 201
    And the cost quote response contains
      | totalCost | subtotal | effectiveDurationMinutes | estimate |
      | 21.00     | 21.00    | 180                      | false    |
    And the confirm return request is
      | operatorId |
      | OP1        |
    When a POST request has been made to "/api/rentals/100/returns" endpoint
    Then the response status is 201
    And the rental return response contains rental
      | customerId | status    | actualDuration | plannedDuration | estimatedCost | totalCost |
      | CUS2       | COMPLETED | 180            | 120             | 16.00         | 21.00     |
    And the rental return response contains rental equipments
      | equipmentId | equipmentUid | status   | tariffId | finalCost | actualReturnAt      |
      | 1           | BIKE-001     | RETURNED | 1        | 21.00     | 2026-02-10T11:30:00 |
    And the rental return response does contain settlement info
    And the following rental completed event was published
      | rentalId | equipmentIds | returnedEquipmentIds | totalCost | returnTime          |
      | 100      | 1            | 1                    | 21.00     | 2026-02-10T11:30:00 |
    And the following sub-ledger records were persisted in db
      | id      | accountId | ledgerType      | balance |
      | L_C_H2  | ACC2      | CUSTOMER_HOLD   | 0.00    |
      | L_C_W2  | ACC2      | CUSTOMER_WALLET | 15.00   |
      | L_S_REV | ACC_S     | REVENUE         | 21.00   |
    And the following transactions were persisted in db
      | customerId | amount | paymentMethod     | operatorId | type    | recordedAt          | sourceId | sourceType |
      | CUS2       | 16.00  | INTERNAL_TRANSFER | OP1        | CAPTURE | 2026-02-10T11:30:00 | 100      | RENTAL     |
      | CUS2       | 5.00   | INTERNAL_TRANSFER | OP1        | CAPTURE | 2026-02-10T11:30:00 | 100      | RENTAL     |

  Scenario: Race regression — advancing the clock after the quote is taken does not change the charged amount
    Given now is "2026-02-10T11:30:00"
    And a single rental exists in the database with the following data
      | id  | customerId | status | estimatedCost | plannedDuration | startedAt           | createdAt           | updatedAt           |
      | 101 | CUS2       | ACTIVE | 16.00         | 120             | 2026-02-10T08:30:00 | 2026-02-10T08:30:00 | 2026-02-10T08:30:00 |
    And rental equipments exist in the database with the following data
      | rentalId | equipmentId | equipmentUid | equipmentType | tariffId | status | startedAt           | expectedReturnAt    | estimatedCost | createdAt           | updatedAt           |
      | 101      | 1           | BIKE-001     | BICYCLE       | 1        | ACTIVE | 2026-02-10T08:30:00 | 2026-02-10T10:30:00 | 16.00         | 2026-02-10T08:30:00 | 2026-02-10T08:30:00 |
    And the following transaction records exist in db
      | id  | type | paymentMethod | amount | customerId | operatorId | sourceType | sourceId | recordedAt          | idempotencyKey |
      | TX2 | HOLD | CASH          | 16.00  | CUS2       | OP1        | RENTAL     | 101      | 2026-02-10T08:30:00 | IDK4           |
    And the equipment items for cost calculation request are
      | equipmentId | equipmentType | returnAt            |
      | 1           | BICYCLE       | 2026-02-10T11:30:00 |
    And the rental cost calculation request is prepared with the following data
      | startAt             | plannedDurationMinutes |
      | 2026-02-10T08:30:00 | 120                    |
    When a POST request has been made to "/api/tariffs/quotes" endpoint
    Then the response status is 201
    And the cost quote response contains
      | totalCost | subtotal | effectiveDurationMinutes | estimate |
      | 21.00     | 21.00    | 180                      | false    |
    Given now is "2026-02-10T11:34:55"
    And the confirm return request is
      | operatorId |
      | OP1        |
    When a POST request has been made to "/api/rentals/101/returns" endpoint
    Then the response status is 201
    And the rental return response contains rental
      | customerId | status    | actualDuration | plannedDuration | estimatedCost | totalCost |
      | CUS2       | COMPLETED | 180            | 120             | 16.00         | 21.00     |
    And the rental return response contains rental equipments
      | equipmentId | equipmentUid | status   | tariffId | finalCost | actualReturnAt      |
      | 1           | BIKE-001     | RETURNED | 1        | 21.00     | 2026-02-10T11:30:00 |
    And the following rental completed event was published
      | rentalId | equipmentIds | returnedEquipmentIds | totalCost | returnTime          |
      | 101      | 1            | 1                    | 21.00     | 2026-02-10T11:30:00 |
    And the following sub-ledger records were persisted in db
      | id      | accountId | ledgerType      | balance |
      | L_C_H2  | ACC2      | CUSTOMER_HOLD   | 0.00    |
      | L_C_W2  | ACC2      | CUSTOMER_WALLET | 15.00   |
      | L_S_REV | ACC_S     | REVENUE         | 21.00   |
    And the following transactions were persisted in db
      | customerId | amount | paymentMethod     | operatorId | type    | recordedAt          | sourceId | sourceType |
      | CUS2       | 16.00  | INTERNAL_TRANSFER | OP1        | CAPTURE | 2026-02-10T11:34:50 | 101      | RENTAL     |
      | CUS2       | 5.00   | INTERNAL_TRANSFER | OP1        | CAPTURE | 2026-02-10T11:34:50 | 101      | RENTAL     |

  Scenario: Confirm against an expired quote is rejected
    Given now is "2026-02-10T11:30:00"
    And a single rental exists in the database with the following data
      | id  | customerId | status | estimatedCost | plannedDuration | startedAt           | createdAt           | updatedAt           |
      | 102 | CUS2       | ACTIVE | 16.00         | 120             | 2026-02-10T08:30:00 | 2026-02-10T08:30:00 | 2026-02-10T08:30:00 |
    And rental equipments exist in the database with the following data
      | rentalId | equipmentId | equipmentUid | equipmentType | tariffId | status | startedAt           | expectedReturnAt    | estimatedCost | createdAt           | updatedAt           |
      | 102      | 1           | BIKE-001     | BICYCLE       | 1        | ACTIVE | 2026-02-10T08:30:00 | 2026-02-10T10:30:00 | 16.00         | 2026-02-10T08:30:00 | 2026-02-10T08:30:00 |
    And the equipment items for cost calculation request are
      | equipmentId | equipmentType | returnAt            |
      | 1           | BICYCLE       | 2026-02-10T11:30:00 |
    And the rental cost calculation request is prepared with the following data
      | startAt             | plannedDurationMinutes |
      | 2026-02-10T08:30:00 | 120                    |
    When a POST request has been made to "/api/tariffs/quotes" endpoint
    Then the response status is 201
    And the cost quote response contains
      | totalCost | subtotal | effectiveDurationMinutes | estimate |
      | 21.00     | 21.00    | 180                      | false    |
    Given now is "2026-02-10T11:36:00"
    And the confirm return request is
      | operatorId |
      | OP1        |
    When a POST request has been made to "/api/rentals/102/returns" endpoint
    Then the response status is 410
    And the response contains
      | path        | value                |
      | $.errorCode | tariff.quote.expired |

  Scenario: Confirming the same quote twice is rejected on the second attempt
    Given now is "2026-02-10T11:30:00"
    And a single rental exists in the database with the following data
      | id  | customerId | status | estimatedCost | plannedDuration | startedAt           | createdAt           | updatedAt           |
      | 200 | CUS2       | ACTIVE | 16.00         | 120             | 2026-02-10T08:30:00 | 2026-02-10T08:30:00 | 2026-02-10T08:30:00 |
    And rental equipments exist in the database with the following data
      | rentalId | equipmentId | equipmentUid | equipmentType | tariffId | status | startedAt           | expectedReturnAt    | estimatedCost | createdAt           | updatedAt           |
      | 200      | 1           | BIKE-001     | BICYCLE       | 1        | ACTIVE | 2026-02-10T08:30:00 | 2026-02-10T10:30:00 | 16.00         | 2026-02-10T08:30:00 | 2026-02-10T08:30:00 |
    And the following transaction records exist in db
      | id  | type | paymentMethod | amount | customerId | operatorId | sourceType | sourceId | recordedAt          | idempotencyKey |
      | TX2 | HOLD | CASH          | 16.00  | CUS2       | OP1        | RENTAL     | 200      | 2026-02-10T08:30:00 | IDK4           |
    And the equipment items for cost calculation request are
      | equipmentId | equipmentType | returnAt            |
      | 1           | BICYCLE       | 2026-02-10T11:30:00 |
    And the rental cost calculation request is prepared with the following data
      | startAt             | plannedDurationMinutes |
      | 2026-02-10T08:30:00 | 120                    |
    When a POST request has been made to "/api/tariffs/quotes" endpoint
    Then the response status is 201
    And the cost quote response contains
      | totalCost | subtotal | effectiveDurationMinutes | estimate |
      | 21.00     | 21.00    | 180                      | false    |
    And the confirm return request is
      | operatorId |
      | OP1        |
    When a POST request has been made to "/api/rentals/200/returns" endpoint
    Then the response status is 201
    And the rental return response contains rental
      | customerId | status    | actualDuration | plannedDuration | totalCost |
      | CUS2       | COMPLETED | 180            | 120             | 21.00     |
    Given a single rental exists in the database with the following data
      | id  | customerId | status | estimatedCost | plannedDuration | startedAt           | createdAt           | updatedAt           |
      | 201 | CUS2       | ACTIVE | 16.00         | 120             | 2026-02-10T08:30:00 | 2026-02-10T08:30:00 | 2026-02-10T08:30:00 |
    And rental equipments exist in the database with the following data
      | rentalId | equipmentId | equipmentUid | equipmentType | tariffId | status | startedAt           | expectedReturnAt    | estimatedCost | createdAt           | updatedAt           |
      | 201      | 1           | BIKE-001     | BICYCLE       | 1        | ACTIVE | 2026-02-10T08:30:00 | 2026-02-10T10:30:00 | 16.00         | 2026-02-10T08:30:00 | 2026-02-10T08:30:00 |
    And the confirm return request is
      | operatorId |
      | OP1        |
    When a POST request has been made to "/api/rentals/201/returns" endpoint
    Then the response status is 409
    And the response contains
      | path        | value                         |
      | $.errorCode | tariff.quote.already_consumed |

  Scenario Outline: Confirm is rejected — <reason>
    Given now is "2026-02-10T11:30:00"
    And a single rental exists in the database with the following data
      | id  | customerId | status | estimatedCost | plannedDuration | discountPercent   | specialTariffId   | specialPrice   | startedAt           | createdAt           | updatedAt           |
      | 400 | CUS2       | ACTIVE | 16.00         | 120             | <discountPercent> | <specialTariffId> | <specialPrice> | 2026-02-10T08:30:00 | 2026-02-10T08:30:00 | 2026-02-10T08:30:00 |
    And rental equipments exist in the database with the following data
      | rentalId | equipmentId | equipmentUid | equipmentType | tariffId | status        | startedAt        | expectedReturnAt    | actualReturnAt        | estimatedCost | createdAt           | updatedAt           |
      | 400      | 1           | BIKE-001     | BICYCLE       | 1        | <equipStatus> | <equipStartedAt> | 2026-02-10T10:30:00 | <equipActualReturnAt> | 16.00         | 2026-02-10T08:30:00 | 2026-02-10T08:30:00 |
    And the equipment items for cost calculation request are
      | equipmentId        | equipmentType | returnAt        |
      | <quoteEquipmentId> | BICYCLE       | <quoteReturnAt> |
    And the rental cost calculation request is prepared with the following data
      | startAt        | plannedDurationMinutes | specialTariffId        | specialPrice        |
      | <quoteStartAt> | <quotePlannedDuration> | <quoteSpecialTariffId> | <quoteSpecialPrice> |
    When a POST request has been made to "/api/tariffs/quotes" endpoint
    Then the response status is 201
    And the cost quote response contains
      | totalCost    | subtotal     | effectiveDurationMinutes | estimate        | specialPricingApplied |
      | <quoteTotal> | <quoteTotal> | <quoteDuration>          | <quoteEstimate> | <quoteSpecialApplied> |
    And the confirm return request is
      | operatorId |
      | OP1        |
    When a POST request has been made to "/api/rentals/400/returns" endpoint
    Then the response status is 409
    And the response contains
      | path            | value                 |
      | $.errorCode     | rental.quote.mismatch |
      | $.params.reason | <reason>              |
    Examples:
      | reason                                                           | discountPercent | specialTariffId | specialPrice | equipStatus | equipStartedAt      | equipActualReturnAt | quoteEquipmentId | quoteStartAt        | quotePlannedDuration | quoteSpecialTariffId | quoteSpecialPrice | quoteReturnAt       | quoteTotal | quoteDuration | quoteEstimate | quoteSpecialApplied |
      | quote is an estimate and cannot settle a final return            |                 |                 |              | ACTIVE      | 2026-02-10T08:30:00 |                     | 1                | 2026-02-10T08:30:00 | 120                  |                      |                   |                     | 16.00      | 120           | true          | false               |
      | rental start time differs from the rental                        |                 |                 |              | ACTIVE      | 2026-02-10T08:30:00 |                     | 1                | 2026-02-10T07:30:00 | 120                  |                      |                   | 2026-02-10T11:30:00 | 24.00      | 240           | false         | false               |
      | planned duration differs from the rental                         |                 |                 |              | ACTIVE      | 2026-02-10T08:30:00 |                     | 1                | 2026-02-10T08:30:00 | 90                   |                      |                   | 2026-02-10T11:30:00 | 21.00      | 180           | false         | false               |
      | discount differs from the rental                                 | 10              |                 |              | ACTIVE      | 2026-02-10T08:30:00 |                     | 1                | 2026-02-10T08:30:00 | 120                  |                      |                   | 2026-02-10T11:30:00 | 21.00      | 180           | false         | false               |
      | special tariff differs from the rental                           |                 | 5               | 10.00        | ACTIVE      | 2026-02-10T08:30:00 |                     | 1                | 2026-02-10T08:30:00 | 120                  |                      |                   | 2026-02-10T11:30:00 | 21.00      | 180           | false         | false               |
      | special price differs from the rental                            |                 | 5               | 10.00        | ACTIVE      | 2026-02-10T08:30:00 |                     | 1                | 2026-02-10T08:30:00 | 120                  | 5                    | 666.00            | 2026-02-10T11:30:00 | 666.00     | 120           | false         | true                |
      | equipment composition differs from the rental                    |                 |                 |              | ACTIVE      | 2026-02-10T08:30:00 |                     | 2                | 2026-02-10T08:30:00 | 120                  |                      |                   | 2026-02-10T11:30:00 | 21.00      | 180           | false         | false               |
      | equipment 1 start time differs from the rental                   |                 |                 |              | ACTIVE      | 2026-02-10T09:00:00 |                     | 1                | 2026-02-10T08:30:00 | 120                  |                      |                   | 2026-02-10T11:30:00 | 21.00      | 180           | false         | false               |
      | equipment 1 was already returned at a different time than quoted |                 |                 |              | RETURNED    | 2026-02-10T08:30:00 | 2026-02-10T10:00:00 | 1                | 2026-02-10T08:30:00 | 120                  |                      |                   | 2026-02-10T11:30:00 | 21.00      | 180           | false         | false               |
