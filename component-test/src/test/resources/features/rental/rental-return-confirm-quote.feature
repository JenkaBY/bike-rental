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

  Scenario: Confirm returns before expected time - captures partial cost, releases remaining hold
    Given now is "2026-02-10T09:30:00"
    And a single rental exists in the database with the following data
      | id  | customerId | status | estimatedCost | plannedDuration | startedAt           | createdAt           | updatedAt           |
      | 110 | CUS2       | ACTIVE | 16.00         | 120             | 2026-02-10T08:30:00 | 2026-02-10T08:30:00 | 2026-02-10T08:30:00 |
    And rental equipments exist in the database with the following data
      | rentalId | equipmentId | equipmentUid | equipmentType | tariffId | status | startedAt           | expectedReturnAt    | estimatedCost | createdAt           | updatedAt           |
      | 110      | 1           | BIKE-001     | BICYCLE       | 1        | ACTIVE | 2026-02-10T08:30:00 | 2026-02-10T10:30:00 | 16.00         | 2026-02-10T08:30:00 | 2026-02-10T08:30:00 |
    And the following transaction records exist in db
      | id  | type | paymentMethod | amount | customerId | operatorId | sourceType | sourceId | recordedAt          | idempotencyKey |
      | TX2 | HOLD | CASH          | 16.00  | CUS2       | OP1        | RENTAL     | 110      | 2026-02-10T08:30:00 | IDK4           |
    And the equipment items for cost calculation request are
      | equipmentId | equipmentType | returnAt            |
      | 1           | BICYCLE       | 2026-02-10T09:30:00 |
    And the rental cost calculation request is prepared with the following data
      | startAt             | plannedDurationMinutes |
      | 2026-02-10T08:30:00 | 120                    |
    When a POST request has been made to "/api/tariffs/quotes" endpoint
    Then the response status is 201
    And the cost quote response contains
      | totalCost | subtotal | effectiveDurationMinutes | estimate |
      | 9.00      | 9.00     | 60                       | false    |
    And the confirm return request is
      | operatorId |
      | OP1        |
    When a POST request has been made to "/api/rentals/110/returns" endpoint
    Then the response status is 201
    And the rental return response contains rental
      | customerId | status    | actualDuration | plannedDuration | estimatedCost | totalCost |
      | CUS2       | COMPLETED | 60             | 120             | 16.00         | 9.00      |
    And the rental return response contains rental equipments
      | equipmentId | equipmentUid | status   | tariffId | finalCost | actualReturnAt      |
      | 1           | BIKE-001     | RETURNED | 1        | 9.00      | 2026-02-10T09:30:00 |
    And the rental return response does contain settlement info
    And the following rental completed event was published
      | rentalId | equipmentIds | returnedEquipmentIds | totalCost | returnTime          |
      | 110      | 1            | 1                    | 9.00      | 2026-02-10T09:30:00 |
    And the following sub-ledger records were persisted in db
      | id      | accountId | ledgerType      | balance |
      | L_C_H2  | ACC2      | CUSTOMER_HOLD   | 0.00    |
      | L_C_W2  | ACC2      | CUSTOMER_WALLET | 27.00   |
      | L_S_REV | ACC_S     | REVENUE         | 9.00    |
    And the following transactions were persisted in db
      | customerId | amount | paymentMethod     | operatorId | type    | recordedAt          | sourceId | sourceType |
      | CUS2       | 9.00   | INTERNAL_TRANSFER | OP1        | CAPTURE | 2026-02-10T09:30:00 | 110      | RENTAL     |
      | CUS2       | 7.00   | INTERNAL_TRANSFER | OP1        | RELEASE | 2026-02-10T09:30:00 | 110      | RENTAL     |
    And the following transaction records were persisted in db
      | subLedger | ledgerType      | direction | amount |
      | L_C_H2    | CUSTOMER_HOLD   | DEBIT     | 9.00   |
      | L_S_REV   | REVENUE         | CREDIT    | 9.00   |
      | L_C_W2    | CUSTOMER_WALLET | CREDIT    | 7.00   |
      | L_C_H2    | CUSTOMER_HOLD   | DEBIT     | 7.00   |

  Scenario: Confirm leaves rental in DEBT status when wallet balance is insufficient for overtime capture
    Given now is "2026-02-10T13:30:00"
    And customers exist in the database with the following data
      | id   | phone       | firstName | lastName | email            | birthDate  | comments |
      | CUS3 | +3706861551 | Jane      | Doe      | jane@example.com | 1922-02-21 | null     |
    And the following account records exist in db
      | id   | accountType | customerId |
      | ACC3 | CUSTOMER    | CUS3       |
    And the following sub-ledger records exist in db
      | id     | accountId | ledgerType      | balance | version | createdAt            | updatedAt            |
      | L_C_W3 | ACC3      | CUSTOMER_WALLET | 10.00   | 1       | 2026-03-27T00:00:00Z | 2026-03-27T00:00:00Z |
      | L_C_H3 | ACC3      | CUSTOMER_HOLD   | 9.00    | 1       | 2026-03-27T00:00:00Z | 2026-03-27T00:00:00Z |
    And a single rental exists in the database with the following data
      | id  | customerId | status | estimatedCost | plannedDuration | startedAt           | createdAt           | updatedAt           |
      | 111 | CUS3       | ACTIVE | 9.00          | 60              | 2026-02-10T08:30:00 | 2026-02-10T08:30:00 | 2026-02-10T08:30:00 |
    And rental equipments exist in the database with the following data
      | rentalId | equipmentId | equipmentUid | equipmentType | tariffId | status | startedAt           | expectedReturnAt    | estimatedCost | createdAt           | updatedAt           |
      | 111      | 1           | BIKE-001     | BICYCLE       | 1        | ACTIVE | 2026-02-10T08:30:00 | 2026-02-10T09:30:00 | 9.00          | 2026-02-10T08:30:00 | 2026-02-10T08:30:00 |
    And the following transaction records exist in db
      | id  | type | paymentMethod | amount | customerId | operatorId | sourceType | sourceId | recordedAt          | idempotencyKey |
      | TX2 | HOLD | CASH          | 9.00   | CUS3       | OP1        | RENTAL     | 111      | 2026-02-10T08:30:00 | IDK4           |
    And the equipment items for cost calculation request are
      | equipmentId | equipmentType | returnAt            |
      | 1           | BICYCLE       | 2026-02-10T13:30:00 |
    And the rental cost calculation request is prepared with the following data
      | startAt             | plannedDurationMinutes |
      | 2026-02-10T08:30:00 | 60                     |
    When a POST request has been made to "/api/tariffs/quotes" endpoint
    Then the response status is 201
    And the cost quote response contains
      | totalCost | subtotal | effectiveDurationMinutes | estimate |
      | 25.00     | 25.00    | 300                      | false    |
    And the confirm return request is
      | operatorId |
      | OP1        |
    When a POST request has been made to "/api/rentals/111/returns" endpoint
    Then the response status is 201
    And the rental return response contains rental
      | customerId | status | actualDuration | plannedDuration | estimatedCost | totalCost |
      | CUS3       | DEBT   | 300            | 60              | 9.00          | 25.00     |
    And the rental return response contains rental equipments
      | equipmentId | equipmentUid | status   | tariffId | finalCost | actualReturnAt      |
      | 1           | BIKE-001     | RETURNED | 1        | 25.00     | 2026-02-10T13:30:00 |
    And the rental return response does not contain settlement info
    And the following rental completed event was published
      | rentalId | equipmentIds | returnedEquipmentIds | totalCost | returnTime          |
      | 111      | 1            | 1                    | 25.00     | 2026-02-10T13:30:00 |
    And the following sub-ledger records were persisted in db
      | id      | accountId | ledgerType      | balance |
      | L_C_H3  | ACC3      | CUSTOMER_HOLD   | 9.00    |
      | L_C_W3  | ACC3      | CUSTOMER_WALLET | 10.00   |
      | L_S_REV | ACC_S     | REVENUE         | 0.00    |

  Scenario: Confirm applies rental discount to the frozen quote total
    Given now is "2026-02-10T10:00:00"
    And a single rental exists in the database with the following data
      | id  | customerId | status | estimatedCost | plannedDuration | discountPercent | startedAt           | createdAt           | updatedAt           |
      | 112 | CUS2       | ACTIVE | 14.40         | 120             | 10              | 2026-02-10T08:00:00 | 2026-02-10T08:00:00 | 2026-02-10T08:00:00 |
    And rental equipments exist in the database with the following data
      | rentalId | equipmentId | equipmentUid | equipmentType | tariffId | status | startedAt           | expectedReturnAt    | estimatedCost | createdAt           | updatedAt           |
      | 112      | 1           | BIKE-001     | BICYCLE       | 1        | ACTIVE | 2026-02-10T08:00:00 | 2026-02-10T10:00:00 | 16.00         | 2026-02-10T08:00:00 | 2026-02-10T08:00:00 |
    And the following transaction records exist in db
      | id  | type | paymentMethod | amount | customerId | operatorId | sourceType | sourceId | recordedAt          | idempotencyKey |
      | TX2 | HOLD | CASH          | 16.00  | CUS2       | OP1        | RENTAL     | 112      | 2026-02-10T08:00:00 | IDK4           |
    And the equipment items for cost calculation request are
      | equipmentId | equipmentType | returnAt            |
      | 1           | BICYCLE       | 2026-02-10T10:00:00 |
    And the rental cost calculation request is prepared with the following data
      | startAt             | plannedDurationMinutes | discountPercent |
      | 2026-02-10T08:00:00 | 120                    | 10              |
    When a POST request has been made to "/api/tariffs/quotes" endpoint
    Then the response status is 201
    And the cost quote response contains
      | totalCost | subtotal | effectiveDurationMinutes | estimate | discountPercent | discountAmount |
      | 14.40     | 16.00    | 120                      | false    | 10              | 1.60           |
    And the confirm return request is
      | operatorId |
      | OP1        |
    When a POST request has been made to "/api/rentals/112/returns" endpoint
    Then the response status is 201
    And the rental return response contains rental
      | customerId | status    | actualDuration | plannedDuration | estimatedCost | totalCost | discountPercent |
      | CUS2       | COMPLETED | 120            | 120             | 14.40         | 14.40     | 10              |
    And the rental return response does contain settlement info
    And the following rental completed event was published
      | rentalId | equipmentIds | returnedEquipmentIds | totalCost | returnTime          |
      | 112      | 1            | 1                    | 14.40     | 2026-02-10T10:00:00 |
    And the following sub-ledger records were persisted in db
      | id      | accountId | ledgerType      | balance |
      | L_C_H2  | ACC2      | CUSTOMER_HOLD   | 0.00    |
      | L_C_W2  | ACC2      | CUSTOMER_WALLET | 21.60   |
      | L_S_REV | ACC_S     | REVENUE         | 14.40   |
    And the following transactions were persisted in db
      | customerId | amount | paymentMethod     | operatorId | type    | recordedAt          | sourceId | sourceType |
      | CUS2       | 14.40  | INTERNAL_TRANSFER | OP1        | CAPTURE | 2026-02-10T10:00:00 | 112      | RENTAL     |
      | CUS2       | 1.60   | INTERNAL_TRANSFER | OP1        | RELEASE | 2026-02-10T10:00:00 | 112      | RENTAL     |
    And the following transaction records were persisted in db
      | subLedger | ledgerType      | direction | amount |
      | L_C_H2    | CUSTOMER_HOLD   | DEBIT     | 1.60   |
      | L_C_W2    | CUSTOMER_WALLET | CREDIT    | 1.60   |
      | L_C_H2    | CUSTOMER_HOLD   | DEBIT     | 14.40  |
      | L_S_REV   | REVENUE         | CREDIT    | 14.40  |

  Scenario: Confirm charges the flat SPECIAL price frozen on the quote
    Given now is "2026-02-10T10:00:00"
    And a single rental exists in the database with the following data
      | id  | customerId | status | specialTariffId | specialPrice | estimatedCost | plannedDuration | startedAt           | createdAt           | updatedAt           |
      | 113 | CUS2       | ACTIVE | 5               | 10.00        | 10.00         | 120             | 2026-02-10T08:00:00 | 2026-02-10T08:00:00 | 2026-02-10T08:00:00 |
    And rental equipments exist in the database with the following data
      | rentalId | equipmentId | equipmentUid | equipmentType | tariffId | status | startedAt           | expectedReturnAt    | estimatedCost | createdAt           | updatedAt           |
      | 113      | 1           | BIKE-001     | BICYCLE       | 1        | ACTIVE | 2026-02-10T08:00:00 | 2026-02-10T10:00:00 | 16.00         | 2026-02-10T08:00:00 | 2026-02-10T08:00:00 |
    And the following transaction records exist in db
      | id  | type | paymentMethod | amount | customerId | operatorId | sourceType | sourceId | recordedAt          | idempotencyKey |
      | TX2 | HOLD | CASH          | 10.00  | CUS2       | OP1        | RENTAL     | 113      | 2026-02-10T08:00:00 | IDK4           |
    And the equipment items for cost calculation request are
      | equipmentId | equipmentType | returnAt            |
      | 1           | BICYCLE       | 2026-02-10T10:00:00 |
    And the rental cost calculation request is prepared with the following data
      | startAt             | plannedDurationMinutes | specialTariffId | specialPrice |
      | 2026-02-10T08:00:00 | 120                    | 5               | 10.00        |
    When a POST request has been made to "/api/tariffs/quotes" endpoint
    Then the response status is 201
    And the cost quote response contains
      | totalCost | subtotal | effectiveDurationMinutes | estimate | specialPricingApplied |
      | 10.00     | 10.00    | 120                      | false    | true                  |
    And the confirm return request is
      | operatorId |
      | OP1        |
    When a POST request has been made to "/api/rentals/113/returns" endpoint
    Then the response status is 201
    And the rental return response contains rental
      | customerId | status    | actualDuration | plannedDuration | estimatedCost | totalCost | specialPrice |
      | CUS2       | COMPLETED | 120            | 120             | 10.00         | 10.00     | 10.00        |
    And the rental return response does contain settlement info
    And the following rental completed event was published
      | rentalId | equipmentIds | returnedEquipmentIds | totalCost | returnTime          |
      | 113      | 1            | 1                    | 10.00     | 2026-02-10T10:00:00 |
    And the following sub-ledger records were persisted in db
      | id      | accountId | ledgerType      | balance |
      | L_C_H2  | ACC2      | CUSTOMER_HOLD   | 6.00    |
      | L_C_W2  | ACC2      | CUSTOMER_WALLET | 20.00   |
      | L_S_REV | ACC_S     | REVENUE         | 10.00   |
    And the following transactions were persisted in db
      | customerId | amount | paymentMethod     | operatorId | type    | recordedAt          | sourceId | sourceType |
      | CUS2       | 10.00  | INTERNAL_TRANSFER | OP1        | CAPTURE | 2026-02-10T10:00:00 | 113      | RENTAL     |
    And the following transaction records were persisted in db
      | subLedger | ledgerType    | direction | amount |
      | L_C_H2    | CUSTOMER_HOLD | DEBIT     | 10.00  |
      | L_S_REV   | REVENUE       | CREDIT    | 10.00  |

  Scenario: Confirm with ZERO SPECIAL price leaves the hold untouched and captures nothing
    Given now is "2026-02-10T10:00:00"
    And a single rental exists in the database with the following data
      | id  | customerId | status | specialTariffId | specialPrice | estimatedCost | plannedDuration | startedAt           | createdAt           | updatedAt           |
      | 114 | CUS2       | ACTIVE | 5               | 0.00         | 0.00          | 120             | 2026-02-10T08:00:00 | 2026-02-10T08:00:00 | 2026-02-10T08:00:00 |
    And rental equipments exist in the database with the following data
      | rentalId | equipmentId | equipmentUid | equipmentType | tariffId | status | startedAt           | expectedReturnAt    | estimatedCost | createdAt           | updatedAt           |
      | 114      | 1           | BIKE-001     | BICYCLE       | 1        | ACTIVE | 2026-02-10T08:00:00 | 2026-02-10T10:00:00 | 16.00         | 2026-02-10T08:00:00 | 2026-02-10T08:00:00 |
    And the equipment items for cost calculation request are
      | equipmentId | equipmentType | returnAt            |
      | 1           | BICYCLE       | 2026-02-10T10:00:00 |
    And the rental cost calculation request is prepared with the following data
      | startAt             | plannedDurationMinutes | specialTariffId | specialPrice |
      | 2026-02-10T08:00:00 | 120                    | 5               | 0.00         |
    When a POST request has been made to "/api/tariffs/quotes" endpoint
    Then the response status is 201
    And the cost quote response contains
      | totalCost | subtotal | effectiveDurationMinutes | estimate | specialPricingApplied |
      | 0.00      | 0.00     | 120                      | false    | true                  |
    And the confirm return request is
      | operatorId |
      | OP1        |
    When a POST request has been made to "/api/rentals/114/returns" endpoint
    Then the response status is 201
    And the rental return response contains rental
      | customerId | status    | actualDuration | plannedDuration | estimatedCost | totalCost | specialPrice |
      | CUS2       | COMPLETED | 120            | 120             | 0.00          | 0.00      | 0.00         |
    And the rental return response does contain settlement info
    And the following rental completed event was published
      | rentalId | equipmentIds | returnedEquipmentIds | totalCost | returnTime          |
      | 114      | 1            | 1                    | 0.00      | 2026-02-10T10:00:00 |
    And the following sub-ledger records were persisted in db
      | id      | accountId | ledgerType      | balance |
      | L_C_H2  | ACC2      | CUSTOMER_HOLD   | 16.00   |
      | L_C_W2  | ACC2      | CUSTOMER_WALLET | 20.00   |
      | L_S_REV | ACC_S     | REVENUE         | 0.00    |
    And there are only 0 transactions in db

  Scenario: Confirm within the free period releases the full hold back to the customer
    Given now is "2026-02-10T08:00:00"
    And a single rental exists in the database with the following data
      | id  | customerId | status | specialTariffId | specialPrice | estimatedCost | plannedDuration | startedAt           | createdAt           | updatedAt           |
      | 115 | CUS2       | ACTIVE | 5               | 0.00         | 16.00         | 120             | 2026-02-10T08:00:00 | 2026-02-10T08:00:00 | 2026-02-10T08:00:00 |
    And rental equipments exist in the database with the following data
      | rentalId | equipmentId | equipmentUid | equipmentType | tariffId | status | startedAt           | expectedReturnAt    | estimatedCost | createdAt           | updatedAt           |
      | 115      | 1           | BIKE-001     | BICYCLE       | 1        | ACTIVE | 2026-02-10T08:00:00 | 2026-02-10T10:00:00 | 16.00         | 2026-02-10T08:00:00 | 2026-02-10T08:00:00 |
    And the following transaction records exist in db
      | id  | type | paymentMethod | amount | customerId | operatorId | sourceType | sourceId | recordedAt          | idempotencyKey |
      | TX2 | HOLD | CASH          | 16.00  | CUS2       | OP1        | RENTAL     | 115      | 2026-02-10T08:00:00 | IDK4           |
    When a GET request has been made to "/api/finance/customers/{customerId}/balances" endpoint with
      | {customerId} |
      | CUS2         |
    Then the response status is 200
    And the balances response contains
      | walletBalance | holdBalance |
      | 20.00         | 16.00       |
    Given now is "2026-02-10T08:03:00"
    And the equipment items for cost calculation request are
      | equipmentId | equipmentType | returnAt            |
      | 1           | BICYCLE       | 2026-02-10T08:03:00 |
    And the rental cost calculation request is prepared with the following data
      | startAt             | plannedDurationMinutes | specialTariffId | specialPrice |
      | 2026-02-10T08:00:00 | 120                    | 5               | 0.00         |
    When a POST request has been made to "/api/tariffs/quotes" endpoint
    Then the response status is 201
    And the cost quote response contains
      | totalCost | subtotal | effectiveDurationMinutes | estimate | specialPricingApplied |
      | 0.00      | 0.00     | 120                      | false    | true                  |
    And the confirm return request is
      | operatorId |
      | OP1        |
    When a POST request has been made to "/api/rentals/115/returns" endpoint
    Then the response status is 201
    And the rental return response contains rental
      | customerId | status    | actualDuration | plannedDuration | estimatedCost | totalCost | specialPrice |
      | CUS2       | COMPLETED | 3              | 120             | 0.00          | 0.00      | 0.00         |
    And the rental return response does contain settlement info
    And the following rental completed event was published
      | rentalId | equipmentIds | returnedEquipmentIds | totalCost | returnTime          |
      | 115      | 1            | 1                    | 0.00      | 2026-02-10T08:03:00 |
    And the following sub-ledger records were persisted in db
      | id      | accountId | ledgerType      | balance |
      | L_C_H2  | ACC2      | CUSTOMER_HOLD   | 0.00    |
      | L_C_W2  | ACC2      | CUSTOMER_WALLET | 36.00   |
      | L_S_REV | ACC_S     | REVENUE         | 0.00    |
    And the following transactions were persisted in db
      | customerId | amount | paymentMethod     | operatorId | type    | recordedAt          | sourceId | sourceType |
      | CUS2       | 16.00  | INTERNAL_TRANSFER | OP1        | RELEASE | 2026-02-10T08:03:00 | 115      | RENTAL     |
    And the following transaction records were persisted in db
      | subLedger | ledgerType      | direction | amount |
      | L_C_H2    | CUSTOMER_HOLD   | DEBIT     | 16.00  |
      | L_C_W2    | CUSTOMER_WALLET | CREDIT    | 16.00  |

  Scenario: Confirm full return of a rental containing equipment added mid-rental - billed per equipment's own window
    Given now is "2026-02-10T10:00:00"
    And the following equipment records exist in db
      | id | serialNumber | uid      | status | type    | model   | conditionNotes | condition |
      | 2  | EQ-002       | BIKE-002 | RENTED | BICYCLE | Model A | Good           | GOOD      |
    And a single rental exists in the database with the following data
      | id  | customerId | status | estimatedCost | plannedDuration | startedAt           | expectedReturnAt    | createdAt           | updatedAt           |
      | 116 | CUS2       | ACTIVE | 25.00         | 120             | 2026-02-10T08:00:00 | 2026-02-10T10:00:00 | 2026-02-10T08:00:00 | 2026-02-10T08:00:00 |
    And rental equipments exist in the database with the following data
      | rentalId | equipmentId | equipmentUid | equipmentType | tariffId | status | startedAt           | expectedReturnAt    | estimatedCost | createdAt           | updatedAt           |
      | 116      | 1           | BIKE-001     | BICYCLE       | 1        | ACTIVE | 2026-02-10T08:00:00 | 2026-02-10T10:00:00 | 16.00         | 2026-02-10T08:00:00 | 2026-02-10T08:00:00 |
      | 116      | 2           | BIKE-002     | BICYCLE       | 1        | ACTIVE | 2026-02-10T09:00:00 | 2026-02-10T10:00:00 | 9.00          | 2026-02-10T09:00:00 | 2026-02-10T09:00:00 |
    And the following transaction records exist in db
      | id  | type | paymentMethod | amount | customerId | operatorId | sourceType | sourceId | recordedAt          | idempotencyKey |
      | TX2 | HOLD | CASH          | 16.00  | CUS2       | OP1        | RENTAL     | 116      | 2026-02-10T08:00:00 | IDK4           |
    And the equipment items for cost calculation request are
      | equipmentId | equipmentType | startAt             | returnAt            |
      | 1           | BICYCLE       |                     | 2026-02-10T10:00:00 |
      | 2           | BICYCLE       | 2026-02-10T09:00:00 | 2026-02-10T10:00:00 |
    And the rental cost calculation request is prepared with the following data
      | startAt             | plannedDurationMinutes |
      | 2026-02-10T08:00:00 | 120                    |
    When a POST request has been made to "/api/tariffs/quotes" endpoint
    Then the response status is 201
    And the cost quote response contains
      | totalCost | subtotal | effectiveDurationMinutes | estimate |
      | 25.00     | 25.00    | 120                      | false    |
    And the confirm return request is
      | operatorId |
      | OP1        |
    When a POST request has been made to "/api/rentals/116/returns" endpoint
    Then the response status is 201
    And the rental return response contains rental
      | customerId | status    | actualDuration | plannedDuration | estimatedCost | totalCost |
      | CUS2       | COMPLETED | 120            | 120             | 25.00         | 25.00     |
    And the rental return response contains rental equipments
      | equipmentId | equipmentUid | status   | tariffId | finalCost |
      | 1           | BIKE-001     | RETURNED | 1        | 16.00     |
      | 2           | BIKE-002     | RETURNED | 1        | 9.00      |
    And the rental return response does contain settlement info
    And the following sub-ledger records were persisted in db
      | id      | accountId | ledgerType      | balance |
      | L_C_H2  | ACC2      | CUSTOMER_HOLD   | 0.00    |
      | L_C_W2  | ACC2      | CUSTOMER_WALLET | 11.00   |
      | L_S_REV | ACC_S     | REVENUE         | 25.00   |
    And the following transactions were persisted in db
      | customerId | amount | paymentMethod     | operatorId | type    | recordedAt          | sourceId | sourceType |
      | CUS2       | 16.00  | INTERNAL_TRANSFER | OP1        | CAPTURE | 2026-02-10T10:00:00 | 116      | RENTAL     |
      | CUS2       | 9.00   | INTERNAL_TRANSFER | OP1        | CAPTURE | 2026-02-10T10:00:00 | 116      | RENTAL     |
    And the following transaction records were persisted in db
      | subLedger | ledgerType      | direction | amount |
      | L_C_H2    | CUSTOMER_HOLD   | DEBIT     | 16.00  |
      | L_S_REV   | REVENUE         | CREDIT    | 16.00  |
      | L_C_W2    | CUSTOMER_WALLET | DEBIT     | 9.00   |
      | L_S_REV   | REVENUE         | CREDIT    | 9.00   |
