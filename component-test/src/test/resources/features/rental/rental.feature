@ReinitializeSystemLedgers
Feature: Rental Management
  As an operator
  I want to create and manage rentals
  So that I can track bike rental operations

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

  Scenario: Create rental draft
    When a POST request has been made to "/api/rentals/draft" endpoint
    Then the response status is 201
    And the rental response only contains
      | status | estimatedCost |
      | DRAFT  | 0             |

  Scenario Outline: Update rental with all required fields (tariff autoselect)
    Given a POST request has been made to "/api/rentals/draft" endpoint
    Then the response status is 201
    And the rental response only contains
      | status |
      | DRAFT  |
#    Update draft rental
    Given a rental request with the following data
      | customerId   | equipmentIds                 | duration          | operatorId |
      | <customerId> | <equipmentId>,<equipmentId2> | <plannedDuration> | OP1        |
    When a PUT request has been made to "/api/rentals/{requestedObjectId}" endpoint with context
    Then the response status is 200
    And the rental response only contains
      | customerId   | status | plannedDuration   | estimatedCost |
      | <customerId> | DRAFT  | <plannedDuration> | 17.00         |
    And the rental response only contains rental equipments
      | equipmentId    | equipmentUid   | status   | estimatedCost |
      | <equipmentId>  | BIKE-001       | ASSIGNED | 16.00         |
      | <equipmentId2> | HELM-ADULT-001 | ASSIGNED | 1.00          |
    #    rental module
    And rental equipments were persisted in database
      | equipmentId    | equipmentUid   | status   | estimatedCost |
      | <equipmentId>  | BIKE-001       | ASSIGNED | 16.00         |
      | <equipmentId2> | HELM-ADULT-001 | ASSIGNED | 1.00          |
    Examples:
      | customerId | equipmentId | equipmentId2 | plannedDuration |
      | CUS1       | 1           | 3            | 120             |

  @ResetClock
  Scenario: Update rental with auto-selected tariff when no suitable tariff found
    Given a single rental exists in the database with the following data
      | customerId | status | createdAt           | updatedAt           |
      | CUS1       | DRAFT  | 2026-02-06T10:00:00 | 2026-02-06T10:00:00 |
    Given today is "2026-02-09"
    And the following equipment records exist in db
      | id | serialNumber | uid       | status    | type  | model   | conditionNotes | condition |
      | 4  | EQ-005       | OTHER-004 | AVAILABLE | OTHER | Other X | Good           | GOOD      |
    And a rental request with the following data
      | customerId | equipmentIds | duration | tariffId | operatorId |
      | CUS1       | 4            | 120      |          | OP1        |
    When a PUT request has been made to "/api/rentals/{requestedObjectId}" endpoint with context
    Then the response status is 404
    And the response contains
      | path                   | value                                                                                            |
      | $.title                | Suitable tariff not found                                                                        |
      | $.detail               | No suitable tariff found for equipment type 'OTHER' on date 2026-02-09 for duration: 120 minutes |
      | $.errorCode            | tariff.suitable.not_found                                                                        |
      | $.params.equipmentType | OTHER                                                                                            |
      | $.params.rentalDate    | 2026-02-09                                                                                       |
      | $.params.duration      | 120                                                                                              |

  # Rental Update Scenarios
  Scenario Outline: Update rental - rental in DRAFT, estimated costs are calculated, ho holds
    Given a single rental exists in the database with the following data
      | customerId | status | createdAt           | updatedAt           |
      | CUS1       | DRAFT  | 2026-02-06T10:00:00 | 2026-02-06T10:00:00 |
    Given a rental request with the following data
      | customerId   | equipmentIds                 | duration          | operatorId |
      | <customerId> | <equipmentId>,<equipmentId2> | <plannedDuration> | OP1        |
    When a PUT request has been made to "/api/rentals/{requestedObjectId}" endpoint with context
    Then the response status is 200
    And the rental response only contains
      | customerId   | status | plannedDuration   | estimatedCost |
      | <customerId> | DRAFT  | <plannedDuration> | 17.00         |
    And the rental response only contains rental equipments
      | equipmentId    | equipmentUid    | status   | estimatedCost |
      | <equipmentId>  | <equipmentUid1> | ASSIGNED | 16.00         |
      | <equipmentId2> | <equipmentUid2> | ASSIGNED | 1.00          |
    And rental was persisted in database
      | customerId   | status | plannedDuration   | createdAt           |
      | <customerId> | DRAFT  | <plannedDuration> | 2026-02-06T10:00:00 |
    And rental equipments were persisted in database
      | equipmentId    | equipmentUid    | status   | estimatedCost | tariffId |
      | <equipmentId>  | <equipmentUid1> | ASSIGNED | 16.00         | 10       |
      | <equipmentId2> | <equipmentUid2> | ASSIGNED | 1             | 11       |
#    doesn't create hold
    And the following sub-ledger records were persisted in db
      | id     | accountId | ledgerType      | balance |
      | L_C_W1 | ACC1      | CUSTOMER_WALLET | 300.00  |
      | L_C_H1 | ACC1      | CUSTOMER_HOLD   | 0.00    |
    Examples:
      | customerId | equipmentId | equipmentId2 | equipmentUid1 | equipmentUid2  | plannedDuration |
      | CUS2       | 1           | 3            | BIKE-001      | HELM-ADULT-001 | 120             |


  Scenario Outline: Update rental - select equipment twice and change duration. Should lead to recalculation planned costs
    Given a single rental exists in the database with the following data
      | customerId | status | createdAt           | updatedAt           |
      | CUS1       | DRAFT  | 2026-02-06T10:00:00 | 2026-02-06T10:00:00 |
    And a rental request with the following data
      | customerId   | equipmentIds                 | duration          | operatorId |
      | <customerId> | <equipmentId>,<equipmentId2> | <plannedDuration> | OP1        |
    When a PUT request has been made to "/api/rentals/{requestedObjectId}" endpoint with context
    Then the response status is 200
    And the rental response only contains
      | customerId   | status | plannedDuration   | estimatedCost |
      | <customerId> | DRAFT  | <plannedDuration> | 17.00         |
    And the rental response only contains rental equipments
      | equipmentId    | equipmentUid    | status   | estimatedCost |
      | <equipmentId>  | <equipmentUid1> | ASSIGNED | 16.00         |
      | <equipmentId2> | <equipmentUid2> | ASSIGNED | 1.00          |
    And rental was persisted in database
      | customerId   | status | plannedDuration   | createdAt           |
      | <customerId> | DRAFT  | <plannedDuration> | 2026-02-06T10:00:00 |
    And rental equipments were persisted in database
      | equipmentId    | equipmentUid    | status   | estimatedCost | tariffId |
      | <equipmentId>  | <equipmentUid1> | ASSIGNED | 16.00         | 10       |
      | <equipmentId2> | <equipmentUid2> | ASSIGNED | 1             | 11       |
#    Second update
    Given a rental request with the following data
      | customerId | equipmentIds  | duration | operatorId |
      | CUS2       | <equipmentId> | 60       | OP1        |
    When a PUT request has been made to "/api/rentals/{requestedObjectId}" endpoint with context
    And the rental response only contains
      | customerId | status | plannedDuration | estimatedCost |
      | CUS2       | DRAFT  | 60              | 9.00          |
    And the rental response only contains rental equipments
      | equipmentId   | equipmentUid    | status   | estimatedCost |
      | <equipmentId> | <equipmentUid1> | ASSIGNED | 9.00          |
    And rental was persisted in database
      | customerId | status | plannedDuration | createdAt           |
      | CUS2       | DRAFT  | 60              | 2026-02-06T10:00:00 |
    And rental equipments were persisted in database
      | equipmentId   | equipmentUid    | status   | estimatedCost | tariffId |
      | <equipmentId> | <equipmentUid1> | ASSIGNED | 9.00          | 10       |
    And there's 1 rental equipment in database
    Examples:
      | customerId | equipmentId | equipmentId2 | equipmentUid1 | equipmentUid2  | plannedDuration |
      | CUS2       | 1           | 3            | BIKE-001      | HELM-ADULT-001 | 120             |

  # Rental Query Scenarios
  Scenario Outline: Get rental by ID
    Given a single rental exists in the database with the following data
      | id         | customerId   | equipmentId   | status   | plannedDuration   | createdAt           | updatedAt           |
      | <rentalId> | <customerId> | <equipmentId> | <status> | <plannedDuration> | 2026-02-06T10:00:00 | 2026-02-06T10:00:00 |
    And rental equipment exists in the database with the following data
      | rentalId   | equipmentId   | equipmentUid   | equipmentType | tariffId   | status   | startedAt           | expectedReturnAt    | estimatedCost | createdAt           |
      | <rentalId> | <equipmentId> | BIKE-001       | BICYCLE       | <tariffId> | ASSIGNED | 2026-02-10T08:00:00 | 2026-02-10T10:00:00 | 200.00        | 2026-02-10T08:00:00 |
      | <rentalId> | 3             | HELM-ADULT-001 | HELMET        | 3          | ASSIGNED | 2026-02-10T08:00:00 | 2026-02-10T10:00:00 | 10.00         | 2026-02-10T08:00:00 |
    When a GET request has been made to "/api/rentals/{requestedObjectId}" endpoint with context
    Then the response status is 200
    And the rental response only contains
      | customerId   | status   | plannedDuration   | estimatedCost |
      | <customerId> | <status> | <plannedDuration> | 210.00        |
    And the rental response only contains rental equipments
      | equipmentId   | equipmentUid   | status   | tariffId   | estimatedCost | finalCost |
      | <equipmentId> | BIKE-001       | ASSIGNED | <tariffId> | 200.00        |           |
      | 3             | HELM-ADULT-001 | ASSIGNED | 3          | 10.00         |           |
    Examples:
      | customerId | equipmentId | tariffId | status | rentalId | plannedDuration |
      | CUS1       | 1           | 1        | DRAFT  | 1        | 60              |

  Scenario: Get rental by non-existent ID
    When a GET request has been made to "/api/rentals/{id}" endpoint with
      | {id} |
      | 999  |
    Then the response status is 404
    And the response contains
      | path     | value                                  |
      | $.title  | Not Found                              |
      | $.detail | Rental with identifier '999' not found |

  Scenario Outline: Update rental with discount — estimated cost reflects discounted total
    Given a single rental exists in the database with the following data
      | customerId | status | createdAt           | updatedAt           |
      | CUS1       | DRAFT  | 2026-02-06T10:00:00 | 2026-02-06T10:00:00 |
    Given a rental request with the following data
      | customerId   | equipmentIds                 | duration          | operatorId | discountPercent |
      | <customerId> | <equipmentId>,<equipmentId2> | <plannedDuration> | OP1        | 10              |
    When a PUT request has been made to "/api/rentals/{requestedObjectId}" endpoint with context
    Then the response status is 200
    And the rental response only contains
      | customerId   | status | plannedDuration   | estimatedCost |
      | <customerId> | DRAFT  | <plannedDuration> | 15.30         |
    And the rental response only contains rental equipments
      | equipmentId    | equipmentUid    | status   | estimatedCost |
      | <equipmentId>  | <equipmentUid1> | ASSIGNED | 16.00         |
      | <equipmentId2> | <equipmentUid2> | ASSIGNED | 1.00          |
    And rental was persisted in database
      | estimatedCost | discountPercent | customerId   | status | plannedDuration   | createdAt           |
      | 15.30         | 10              | <customerId> | DRAFT  | <plannedDuration> | 2026-02-06T10:00:00 |
    And rental equipments were persisted in database
      | equipmentId    | equipmentUid    | status   | estimatedCost | tariffId |
      | <equipmentId>  | <equipmentUid1> | ASSIGNED | 16.00         | 10       |
      | <equipmentId2> | <equipmentUid2> | ASSIGNED | 1             | 11       |
    Examples:
      | customerId | equipmentId | equipmentId2 | equipmentUid1 | equipmentUid2  | plannedDuration |
      | CUS2       | 1           | 3            | BIKE-001      | HELM-ADULT-001 | 120             |

  Scenario Outline: Update rental with SPECIAL tariff — specialPrice is used as total
    Given a single rental exists in the database with the following data
      | customerId | status | createdAt           | updatedAt           |
      | CUS1       | DRAFT  | 2026-02-06T10:00:00 | 2026-02-06T10:00:00 |
    Given a rental request with the following data
      | customerId   | equipmentIds                 | duration          | operatorId | specialTariffId | specialPrice   |
      | <customerId> | <equipmentId>,<equipmentId2> | <plannedDuration> | OP1        | 13              | <specialPrice> |
    When a PUT request has been made to "/api/rentals/{requestedObjectId}" endpoint with context
    Then the response status is 200
    And the rental response only contains
      | customerId   | status | plannedDuration   | estimatedCost  |
      | <customerId> | DRAFT  | <plannedDuration> | <specialPrice> |
    And the rental response only contains rental equipments
      | equipmentId    | equipmentUid    | status   | estimatedCost |
      | <equipmentId>  | <equipmentUid1> | ASSIGNED | 0             |
      | <equipmentId2> | <equipmentUid2> | ASSIGNED | 0             |
    And rental was persisted in database
      | estimatedCost  | specialTariffId | specialPrice   | customerId   | status | plannedDuration   | createdAt           |
      | <specialPrice> | 13              | <specialPrice> | <customerId> | DRAFT  | <plannedDuration> | 2026-02-06T10:00:00 |
    And rental equipments were persisted in database
      | equipmentId    | equipmentUid    | status   | estimatedCost | tariffId |
      | <equipmentId>  | <equipmentUid1> | ASSIGNED | 0             |          |
      | <equipmentId2> | <equipmentUid2> | ASSIGNED | 0             |          |
    Examples:
      | customerId | equipmentId | equipmentId2 | equipmentUid1 | equipmentUid2  | plannedDuration | specialPrice |
      | CUS2       | 1           | 3            | BIKE-001      | HELM-ADULT-001 | 120             | 11.33        |
      | CUS2       | 1           | 3            | BIKE-001      | HELM-ADULT-001 | 120             | 0            |

  Scenario: Rejected — specialTariffId references a non-SPECIAL tariff type
    Given a single rental exists in the database with the following data
      | customerId | status | createdAt           | updatedAt           |
      | CUS1       | DRAFT  | 2026-02-06T10:00:00 | 2026-02-06T10:00:00 |
    And a rental request with the following data
      | customerId | equipmentIds | duration | operatorId | specialTariffId | specialPrice |
      | CUS1       | 1            | 500      | OP1        | 10              | 12           |
    When a PUT request has been made to "/api/rentals/{requestedObjectId}" endpoint with context
    Then the response status is 422
    And the response contains
      | path        | value                       |
      | $.errorCode | tariff.special.type_invalid |