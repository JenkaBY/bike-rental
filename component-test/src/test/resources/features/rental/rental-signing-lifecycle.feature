@ReinitializeSystemLedgers
Feature: Rental Signing Lifecycle
  As an operator
  I want to move a prepared draft into AWAITING_SIGNATURE and back
  So that funds are held while the customer signs and the composition stays frozen

  Background:
    Given the request header "Content-Type" is "application/vnd.bikerental.v1+json"
    And customers exist in the database with the following data
      | id   | phone        | firstName | lastName | email            | birthDate  | comments |
      | CUS1 | +79995551111 | Alex      | Johnson  | null             | null       | null     |
      | CUS2 | +79991232222 | John      | Doe      | john@example.com | 1922-02-22 | null     |
    And the following equipment statues exist in the database
      | slug      | name      | description    | transitions      |
      | AVAILABLE | Available | Ready to rent  | RENTED,RESERVED  |
      | RESERVED  | Reserved  | Ready to rent  | AVAILABLE,RENTED |
      | RENTED    | Rented    | In use already | AVAILABLE        |
    And the following equipment types exist in the database
      | slug    | name    | description |
      | BICYCLE | Bicycle | Two-wheeled |
      | SCOOTER | Scooter | Scooter     |
      | HELMET  | Helmet  | Helmet      |
      | OTHER   | Other   | Other       |
    And the following equipment records exist in db
      | id | serialNumber | uid            | status    | type    | model   | conditionNotes | condition |
      | 1  | EQ-001       | BIKE-001       | AVAILABLE | BICYCLE | Model A | Good           | GOOD      |
      | 2  | EQ-002       | E-BIKE-001     | AVAILABLE | SCOOTER | Model B | Excellent      | GOOD      |
      | 3  | EQ-003       | HELM-ADULT-001 | AVAILABLE | HELMET  | Model B | Excellent      | GOOD      |
    And the following account records exist in db
      | id   | accountType | customerId |
      | ACC1 | CUSTOMER    | CUS1       |
      | ACC2 | CUSTOMER    | CUS2       |
    And the following sub-ledger records exist in db
      | id     | accountId | ledgerType      | balance | version | createdAt            | updatedAt            |
      | L_C_W1 | ACC1      | CUSTOMER_WALLET | 300.00  | 2       | 2026-03-27T00:00:00Z | 2026-04-07T10:31:02Z |
      | L_C_H1 | ACC1      | CUSTOMER_HOLD   | 0.00    | 2       | 2026-03-27T00:00:00Z | 2026-04-07T10:30:00Z |
      | L_C_W2 | ACC2      | CUSTOMER_WALLET | 300.00  | 1       | 2026-03-27T00:00:00Z | 2026-03-27T00:00:00Z |
      | L_C_H2 | ACC2      | CUSTOMER_HOLD   | 0.00    | 1       | 2026-03-27T00:00:00Z | 2026-03-27T00:00:00Z |
    And the following transaction records exist in db
      | id  | type    | paymentMethod | amount | customerId | operatorId | sourceType | sourceId | recordedAt          | idempotencyKey |
      | TX1 | DEPOSIT | CASH          | 300.00 | CUS1       | OP1        |            |          | 2026-01-10T10:00:00 | IDK1           |
    And the pricing params list for tariff request is
      | tariffId | pricingType       | firstHourPrice | hourlyDiscount | minimumHourlyPrice | hourlyPrice | dailyPrice | overtimeHourlyPrice | issuanceFee | minimumDurationMinutes | minimumDurationSurcharge | price |
      | 10       | DEGRESSIVE_HOURLY | 9.00           | 2.00           | 1.00               |             |            |                     |             | 30                     | 1.00                     |       |
      | 13       | SPECIAL           |                |                |                    |             |            |                     |             |                        |                          | 0     |
    And the following tariff v2 records exist in db
      | id | name                   | description               | equipmentType | pricingType       | status | validFrom  | validTo |
      | 10 | Degressive Hourly Bike | Bicycle degressive hourly | BICYCLE       | DEGRESSIVE_HOURLY | ACTIVE | 2026-01-01 |         |
      | 13 | Special Group Tariff   | Special zero price        | ANY           | SPECIAL           | ACTIVE | 2025-01-01 |         |

  Scenario: Prepare signing holds funds and freezes the rental
    Given a single rental exists in the database with the following data
      | id | customerId | status | plannedDuration | createdAt           | updatedAt           |
      | 1  | CUS1       | DRAFT  | 120             | 2026-04-28T09:00:00 | 2026-04-28T09:00:00 |
    And rental equipments exist in the database with the following data
      | rentalId | equipmentId | equipmentUid | equipmentType | tariffId | status   | startedAt           | expectedReturnAt    | estimatedCost | createdAt           | updatedAt           |
      | 1        | 1           | BIKE-001     | BICYCLE       | 10       | ASSIGNED | 2026-04-28T09:00:00 | 2026-04-28T11:00:00 | 16.00         | 2026-04-28T09:00:00 | 2026-04-28T09:00:00 |
    And the lifecycle request is
      | status             | operatorId |
      | AWAITING_SIGNATURE | OP1        |
    When a PATCH request has been made to "/api/rentals/{requestedObjectId}/lifecycles" endpoint with context
    Then the response status is 200
    And the rental response only contains
      | customerId | status             | estimatedCost | plannedDuration | startedAt |
      | CUS1       | AWAITING_SIGNATURE | 16.00         | 120             |           |
    And rental was persisted in database
      | customerId | status             |
      | CUS1       | AWAITING_SIGNATURE |
    And the following sub-ledger records were persisted in db
      | id     | accountId | ledgerType      | balance |
      | L_C_W1 | ACC1      | CUSTOMER_WALLET | 284.00  |
      | L_C_H1 | ACC1      | CUSTOMER_HOLD   | 16.00   |

  Scenario: Composition editing is rejected while awaiting signature
    Given a single rental exists in the database with the following data
      | id | customerId | status             | plannedDuration | createdAt           | updatedAt           |
      | 1  | CUS1       | AWAITING_SIGNATURE | 120             | 2026-04-28T09:00:00 | 2026-04-28T09:00:00 |
    And rental equipments exist in the database with the following data
      | rentalId | equipmentId | equipmentUid | equipmentType | tariffId | status   | startedAt           | expectedReturnAt    | estimatedCost | createdAt           | updatedAt           |
      | 1        | 1           | BIKE-001     | BICYCLE       | 10       | ASSIGNED | 2026-04-28T09:00:00 | 2026-04-28T11:00:00 | 16.00         | 2026-04-28T09:00:00 | 2026-04-28T09:00:00 |
    And a rental request with the following data
      | customerId | equipmentIds | duration | operatorId |
      | CUS1       | 1,2          | 120      | OP1        |
    When a PUT request has been made to "/api/rentals/{requestedObjectId}" endpoint with context
    Then the response status is 422
    And the response contains
      | path        | value                 |
      | $.errorCode | rental.status.invalid |
    And rental equipments were persisted in database
      | rentalId | equipmentId | equipmentUid | status   |
      | 1        | 1           | BIKE-001     | ASSIGNED |

  Scenario: Cancel signing returns to draft and releases the hold
    Given a single rental exists in the database with the following data
      | id | customerId | status | plannedDuration | createdAt           | updatedAt           |
      | 1  | CUS1       | DRAFT  | 120             | 2026-04-28T09:00:00 | 2026-04-28T09:00:00 |
    And rental equipments exist in the database with the following data
      | rentalId | equipmentId | equipmentUid | equipmentType | tariffId | status   | startedAt           | expectedReturnAt    | estimatedCost | createdAt           | updatedAt           |
      | 1        | 1           | BIKE-001     | BICYCLE       | 10       | ASSIGNED | 2026-04-28T09:00:00 | 2026-04-28T11:00:00 | 16.00         | 2026-04-28T09:00:00 | 2026-04-28T09:00:00 |
    And the lifecycle request is
      | status             | operatorId |
      | AWAITING_SIGNATURE | OP1        |
    When a PATCH request has been made to "/api/rentals/{requestedObjectId}/lifecycles" endpoint with context
    Then the response status is 200
    And the rental response version is greater than 0
    And the following sub-ledger records were persisted in db
      | id     | accountId | ledgerType    | balance |
      | L_C_H1 | ACC1      | CUSTOMER_HOLD | 16.00   |
    And the lifecycle request is
      | status | operatorId |
      | DRAFT  | OP1        |
    When a PATCH request has been made to "/api/rentals/{requestedObjectId}/lifecycles" endpoint with context
    Then the response status is 200
    And the rental response only contains
      | customerId | status | plannedDuration |
      | CUS1       | DRAFT  | 120             |
    And the following sub-ledger records were persisted in db
      | id     | accountId | ledgerType      | balance |
      | L_C_W1 | ACC1      | CUSTOMER_WALLET | 300.00  |
      | L_C_H1 | ACC1      | CUSTOMER_HOLD   | 0.00    |
    And the lifecycle request is
      | status             | operatorId |
      | AWAITING_SIGNATURE | OP1        |
    When a PATCH request has been made to "/api/rentals/{requestedObjectId}/lifecycles" endpoint with context
    Then the response status is 200
    And the rental response only contains
      | customerId | status             | plannedDuration |
      | CUS1       | AWAITING_SIGNATURE | 120             |
    And the rental response version is greater than 2

  Scenario: Prepare signing rejected for incomplete draft
    Given a single rental exists in the database with the following data
      | id | customerId | status | plannedDuration | createdAt           | updatedAt           |
      | 1  |            | DRAFT  | 120             | 2026-04-28T09:00:00 | 2026-04-28T09:00:00 |
    And rental equipments exist in the database with the following data
      | rentalId | equipmentId | equipmentUid | equipmentType | tariffId | status   | startedAt           | expectedReturnAt    | estimatedCost | createdAt           | updatedAt           |
      | 1        | 1           | BIKE-001     | BICYCLE       | 10       | ASSIGNED | 2026-04-28T09:00:00 | 2026-04-28T11:00:00 | 16.00         | 2026-04-28T09:00:00 | 2026-04-28T09:00:00 |
    And the lifecycle request is
      | status             | operatorId |
      | AWAITING_SIGNATURE | OP1        |
    When a PATCH request has been made to "/api/rentals/{requestedObjectId}/lifecycles" endpoint with context
    Then the response status is 422
    And the response contains
      | path        | value                       |
      | $.errorCode | rental.activation.not_ready |
    And the following sub-ledger records were persisted in db
      | id     | accountId | ledgerType    | balance |
      | L_C_H1 | ACC1      | CUSTOMER_HOLD | 0.00    |

  Scenario: Zero-cost rental holds nothing
    Given a single rental exists in the database with the following data
      | id | customerId | status | plannedDuration | specialTariffId | specialPrice | createdAt           | updatedAt           |
      | 1  | CUS1       | DRAFT  | 120             | 13              | 0.00         | 2026-04-28T09:00:00 | 2026-04-28T09:00:00 |
    And rental equipments exist in the database with the following data
      | rentalId | equipmentId | equipmentUid | equipmentType | tariffId | status   | startedAt           | expectedReturnAt    | estimatedCost | createdAt           | updatedAt           |
      | 1        | 1           | BIKE-001     | BICYCLE       | 13       | ASSIGNED | 2026-04-28T09:00:00 | 2026-04-28T11:00:00 | 0.00          | 2026-04-28T09:00:00 | 2026-04-28T09:00:00 |
    And the lifecycle request is
      | status             | operatorId |
      | AWAITING_SIGNATURE | OP1        |
    When a PATCH request has been made to "/api/rentals/{requestedObjectId}/lifecycles" endpoint with context
    Then the response status is 200
    And the rental response only contains
      | customerId | status             | estimatedCost | plannedDuration |
      | CUS1       | AWAITING_SIGNATURE | 0             | 120             |
    And the following sub-ledger records were persisted in db
      | id     | accountId | ledgerType    | balance |
      | L_C_H1 | ACC1      | CUSTOMER_HOLD | 0.00    |
