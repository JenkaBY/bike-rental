@ReinitializeSystemLedgers
Feature: Rental Creation In Awaiting Signing status
  As an operator
  I want to create AWAITING_SIGNATURE rentals
  So that I can track bike rental operations

  Background:
    Given the request header "Content-Type" is "application/vnd.bikerental.v1+json"
    And customers exist in the database with the following data
      | id   | phone        | firstName | lastName | email            | birthDate  | comments |
      | CUS1 | +79995551111 | Alex      | Johnson  | null             | null       | null     |
      | CUS2 | +79991232222 | John      | Doe      | john@example.com | 1922-02-22 | null     |
    And the following equipment types exist in the database
      | slug    | name    | description |
      | BICYCLE | Bicycle | Two-wheeled |
      | SCOOTER | Scooter | Scooter     |
      | HELMET  | Helmet  | Helmet      |
      | OTHER   | Other   | Other       |
    And the following equipment records exist in db
      | id | serialNumber | uid            | type    | model   | conditionNotes | condition |
      | 1  | EQ-001       | BIKE-001       | BICYCLE | Model A | Good           | GOOD      |
      | 2  | EQ-002       | E-BIKE-001     | SCOOTER | Model B | Excellent      | GOOD      |
      | 3  | EQ-003       | HELM-ADULT-001 | HELMET  | Model B | Excellent      | GOOD      |
    And the following account records exist in db
      | id   | accountType | customerId |
      | ACC1 | CUSTOMER    | CUS1       |
      | ACC2 | CUSTOMER    | CUS2       |
    And the following sub-ledger records exist in db
      | id     | accountId | ledgerType      | balance | version | createdAt            | updatedAt            |
      | L_C_W1 | ACC1      | CUSTOMER_WALLET | 300.00  | 2       | 2026-03-27T00:00:00Z | 2026-04-07T10:31:02Z |
      | L_C_H1 | ACC1      | CUSTOMER_HOLD   | 0.00    | 2       | 2026-03-27T00:00:00Z | 2026-04-07T10:30:00Z |
      | L_C_W2 | ACC2      | CUSTOMER_WALLET | 10.00   | 1       | 2026-03-27T00:00:00Z | 2026-03-27T00:00:00Z |
      | L_C_H2 | ACC2      | CUSTOMER_HOLD   | 0.00    | 1       | 2026-03-27T00:00:00Z | 2026-03-27T00:00:00Z |
    And the following transaction records exist in db
      | id  | type    | paymentMethod | amount | customerId | operatorId | sourceType | sourceId | recordedAt          | idempotencyKey |
      | TX1 | DEPOSIT | CASH          | 300.00 | CUS1       | OP1        |            |          | 2026-01-10T10:00:00 | IDK1           |
      | TX4 | DEPOSIT | CASH          | 10.00  | CUS2       | OP1        | RENTAL     | RENT2    | 2026-03-21T10:00:00 | IDK4           |
    And the pricing params list for tariff request is
      | tariffId | pricingType       | firstHourPrice | hourlyDiscount | minimumHourlyPrice | hourlyPrice | dailyPrice | overtimeHourlyPrice | issuanceFee | minimumDurationMinutes | minimumDurationSurcharge | price |
      | 10       | DEGRESSIVE_HOURLY | 9.00           | 2.00           | 1.00               |             |            |                     |             | 30                     | 1.00                     |       |
      | 11       | FLAT_FEE          |                |                |                    |             |            |                     | 1.00        |                        |                          |       |
      | 12       | FLAT_HOURLY       |                |                |                    | 15.00       |            |                     |             | 30                     | 1.00                     |       |
      | 13       | SPECIAL           |                |                |                    |             |            |                     |             |                        |                          | 0     |
    And the following tariff v2 records exist in db
      | id | name                   | description                | equipmentType | pricingType       | status | validFrom  | validTo |
      | 10 | Degressive Hourly Bike | Bicycle degressive hourly  | BICYCLE       | DEGRESSIVE_HOURLY | ACTIVE | 2026-01-01 |         |
      | 11 | Flat Fee Helmet        | Helmet flat fee            | HELMET        | FLAT_FEE          | ACTIVE | 2026-01-01 |         |
      | 12 | Flat Hourly Scooter    | Scooter flat hourly rate   | SCOOTER       | FLAT_HOURLY       | ACTIVE | 2026-01-01 |         |
      | 13 | Special Group Tariff   | Special pricing for groups | ANY           | SPECIAL           | ACTIVE | 2025-01-01 |         |

  Scenario: Create rental directly in AWAITING_SIGNATURE holds funds in one call
    Given a rental request with the following data
      | customerId | equipmentIds | duration | operatorId |
      | CUS1       | 1            | 120      | OP1        |
    When a POST request has been made to "/api/rentals/awaiting-signature" endpoint
    Then the response status is 201
    And the rental response only contains
      | customerId | status             | estimatedCost | plannedDuration | startedAt |
      | CUS1       | AWAITING_SIGNATURE | 16.00         | 120             |           |
    And the rental response version is greater than 0
    And rental was persisted in database
      | customerId | status             |
      | CUS1       | AWAITING_SIGNATURE |
    And the following sub-ledger records were persisted in db
      | id     | accountId | ledgerType      | balance |
      | L_C_W1 | ACC1      | CUSTOMER_WALLET | 284.00  |
      | L_C_H1 | ACC1      | CUSTOMER_HOLD   | 16.00   |

  Scenario: Create zero-cost rental directly in AWAITING_SIGNATURE holds nothing
    Given a rental request with the following data
      | customerId | equipmentIds | duration | operatorId | specialTariffId | specialPrice |
      | CUS1       | 1            | 120      | OP1        | 13              | 0.00         |
    When a POST request has been made to "/api/rentals/awaiting-signature" endpoint
    Then the response status is 201
    And the rental response only contains
      | customerId | status             | estimatedCost | plannedDuration |
      | CUS1       | AWAITING_SIGNATURE | 0             | 120             |
    And the following sub-ledger records were persisted in db
      | id     | accountId | ledgerType    | balance |
      | L_C_H1 | ACC1      | CUSTOMER_HOLD | 0.00    |

  Scenario Outline: Create AWAITING_SIGNATURE rental with special price
    Given a rental request with the following data
      | customerId   | equipmentIds                 | duration          | operatorId | discountPercent   | specialTariffId   | specialPrice   |
      | <customerId> | <equipmentId>,<equipmentId2> | <plannedDuration> | OP1        | <discountPercent> | <specialTariffId> | <specialPrice> |
    When a POST request has been made to "/api/rentals/awaiting-signature" endpoint
    Then the response status is 201
    And the rental response only contains
      | customerId   | status             | plannedDuration   | estimatedCost   | discountPercent   | specialTariffId   | specialPrice   |
      | <customerId> | AWAITING_SIGNATURE | <plannedDuration> | <estimatedCost> | <discountPercent> | <specialTariffId> | <specialPrice> |
    And the rental response only contains rental equipments
      | equipmentId    | equipmentUid   | status   | estimatedCost |
      | <equipmentId>  | BIKE-001       | ASSIGNED | 0.00          |
      | <equipmentId2> | HELM-ADULT-001 | ASSIGNED | 0.00          |
    #    rental module
    And rental equipments were persisted in database
      | equipmentId    | equipmentUid   | status   | estimatedCost |
      | <equipmentId>  | BIKE-001       | ASSIGNED | 0.00          |
      | <equipmentId2> | HELM-ADULT-001 | ASSIGNED | 0.00          |
    And the following sub-ledger records were persisted in db
      | id     | accountId | ledgerType    | balance |
      | L_C_H1 | ACC1      | CUSTOMER_HOLD | 1.00    |
    Examples:
      | customerId | equipmentId | equipmentId2 | plannedDuration | estimatedCost | discountPercent | specialTariffId | specialPrice |
      | CUS1       | 1           | 3            | 120             | 1             |                 | 13              | 1            |

  Scenario Outline: Update AWAITING_SIGNATURE rental should throw exception
    Given a single rental exists in the database with the following data
      | customerId | status             | createdAt           | updatedAt           |
      | CUS1       | AWAITING_SIGNATURE | 2026-02-06T10:00:00 | 2026-02-06T10:00:00 |
    Given a rental request with the following data
      | customerId   | equipmentIds                 | duration          | operatorId |
      | <customerId> | <equipmentId>,<equipmentId2> | <plannedDuration> | OP1        |
    When a PUT request has been made to "/api/rentals/{requestedObjectId}" endpoint with context
    Then the response status is 422
    And the response contains
      | path        | value                                                                                     |
      | $.title     | Invalid rental status                                                                     |
      | $.detail    | Cannot perform operation on rental with status AWAITING_SIGNATURE. Expected status: DRAFT |
      | $.errorCode | rental.status.invalid                                                                     |
    Examples:
      | customerId | equipmentId | equipmentId2 | plannedDuration |
      | CUS2       | 1           | 3            | 120             |