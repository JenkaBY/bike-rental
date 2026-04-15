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
      | bicycle | Bicycle | Two-wheeled |
      | scooter | Scooter | Scooter     |
      | helmet  | Helmet  | Helmet      |
      | other   | Other   | Other       |
    And the following equipment records exist in db
      | id | serialNumber | uid            | status    | type    | model   | condition |
      | 1  | EQ-001       | BIKE-001       | AVAILABLE | bicycle | Model A | Good      |
      | 2  | EQ-002       | E-BIKE-001     | AVAILABLE | scooter | Model B | Excellent |
      | 3  | EQ-003       | HELM-ADULT-001 | AVAILABLE | helmet  | Model B | Excellent |
    And the following tariff records exist in db
      | id | name           | description             | equipmentType | basePrice | halfHourPrice | hourPrice | dayPrice | discountedPrice | validFrom  | validTo    | status |
      | 1  | Hourly Rate    | Standard hourly         | bicycle       | 100.00    | 60.00         | 100.00    | 500.00   | 90.00           | 2026-01-01 | 2036-12-31 | ACTIVE |
      | 2  | Daily Rate     | Standard daily          | bicycle       | 200.00    | 70.00         | 110.00    | 600.00   | 95.00           | 2026-01-01 | 2036-12-31 | ACTIVE |
      | 3  | Helmet Hourly  | Helmet Standard hourly  | helmet        | 10.00     | 5.00          | 10.00     | 80.00    | 10.00           | 2026-01-01 | 2036-12-31 | ACTIVE |
      | 4  | Scooter Hourly | Scooter Standard hourly | scooter       | 300.00    | 150.00        | 300.00    | 80.00    | 10.00           | 2026-01-01 | 2036-12-31 | ACTIVE |
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
      | 10 | Degressive Hourly Bike | Bicycle degressive hourly  | bicycle       | DEGRESSIVE_HOURLY | ACTIVE | 2026-01-01 |         |
      | 11 | Flat Fee Helmet        | Helmet flat fee            | helmet        | FLAT_FEE          | ACTIVE | 2026-01-01 |         |
      | 12 | Flat Hourly Scooter    | Scooter flat hourly rate   | scooter       | FLAT_HOURLY       | ACTIVE | 2026-01-01 |         |
      | 13 | Special Group Tariff   | Special pricing for groups | any           | SPECIAL           | ACTIVE | 2025-01-01 |         |

  Scenario: Create rental draft
    When a POST request has been made to "/api/rentals/draft" endpoint
    Then the response status is 201
    And the rental response only contains
      | status |
      | DRAFT  |
    And the following rental created event was published
      | status |
      | DRAFT  |

  Scenario Outline: Create rental with all required fields (tariff autoselect)
    Given a rental request with the following data
      | customerId   | equipmentIds                 | duration   | operatorId |
      | <customerId> | <equipmentId>,<equipmentId2> | <duration> | OP1        |
    When a POST request has been made to "/api/rentals" endpoint
    Then the response status is 201
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
    And the following rental created event was published
      | customerId   | eqIds                        | status |
      | <customerId> | <equipmentId>,<equipmentId2> | DRAFT  |
#    equipment module
    And the following equipment records were persisted in db
      | id             | serialNumber | uid            | status   | type    | model   | condition |
      | <equipmentId>  | EQ-001       | BIKE-001       | RESERVED | bicycle | Model A | Good      |
      | <equipmentId2> | EQ-003       | HELM-ADULT-001 | RESERVED | helmet  | Model B | Excellent |
    Examples:
      | customerId | equipmentId | equipmentId2 | duration | plannedDuration |
      | CUS1       | 1           | 3            | PT2H     | 120             |

  @ResetClock
  Scenario: Create rental with auto-selected tariff when no suitable tariff found
    Given today is "2026-02-09"
    And the following equipment records exist in db
      | id | serialNumber | uid       | status    | type  | model   | condition |
      | 4  | EQ-005       | OTHER-004 | AVAILABLE | other | Other X | Good      |
    And a rental request with the following data
      | customerId | equipmentIds | duration | tariffId | operatorId |
      | CUS1       | 4            | PT2H     |          | OP1        |
    When a POST request has been made to "/api/rentals" endpoint
    Then the response status is 404
    And the response contains
      | path                   | value                                                                                            |
      | $.title                | Suitable tariff not found                                                                        |
      | $.detail               | No suitable tariff found for equipment type 'other' on date 2026-02-09 for duration: 120 minutes |
      | $.errorCode            | tariff.suitable.not_found                                                                        |
      | $.params.equipmentType | other                                                                                            |
      | $.params.rentalDate    | 2026-02-09                                                                                       |
      | $.params.duration      | PT2H                                                                                             |

  # Rental Update Scenarios
  Scenario: Update rental - select customer
    Given a single rental exists in the database with the following data
      | customerId | status | createdAt           | updatedAt           |
      | CUS1       | DRAFT  | 2026-02-06T10:00:00 | 2026-02-06T10:00:00 |
    And the rental update request is
      | op      | path        | value |
      | replace | /customerId | CUS2  |
    When a PATCH request has been made to "/api/rentals/{requestedObjectId}" endpoint with context
    Then the response status is 200
    And the rental response only contains
      | customerId | status |
      | CUS2       | DRAFT  |
    And rental was persisted in database
      | customerId | status | createdAt           |
      | CUS2       | DRAFT  | 2026-02-06T10:00:00 |

  Scenario Outline: Update rental - select equipment
    Given a single rental exists in the database with the following data
      | id         | customerId   | plannedDuration   | status | createdAt           | updatedAt           |
      | <rentalId> | <customerId> | <plannedDuration> | DRAFT  | 2026-02-06T10:00:00 | 2026-02-06T10:00:00 |
    And rental equipments exist in the database with the following data
      | rentalId   | equipmentId | equipmentUid   | equipmentType | tariffId | status   | startedAt           | expectedReturnAt    | estimatedCost | createdAt           | updatedAt           |
      | <rentalId> | 1           | BIKE-001       | bicycle       | 1        | ASSIGNED | 2026-02-10T08:00:00 | 2026-02-10T10:00:00 | 200.00        | 2026-02-10T08:00:00 | 2026-02-10T08:00:00 |
      | <rentalId> | 3           | HELM-ADULT-001 | helm          | 3        | ASSIGNED | 2026-02-10T08:00:00 | 2026-02-10T10:00:00 | 20.00         | 2026-02-10T08:00:00 | 2026-02-10T08:00:00 |
    And the rental update request is
      | op      | path          | value |
      | replace | /equipmentIds | [2]   |
    When a PATCH request has been made to "/api/rentals/{requestedObjectId}" endpoint with context
    Then the response status is 200
    And the rental response only contains
      | customerId   | status | plannedDuration   | estimatedCost |
      | <customerId> | DRAFT  | <plannedDuration> | 15.00         |
    And the rental response only contains rental equipments
      | equipmentId | equipmentUid | status   | estimatedCost | finalCost |
      | 2           | E-BIKE-001   | ASSIGNED | 15.00         |           |
    And the following equipment records were persisted in db
      | id | serialNumber | uid            | status    | type    | model   | condition |
      | 1  | EQ-001       | BIKE-001       | AVAILABLE | bicycle | Model A | Good      |
      | 2  | EQ-002       | E-BIKE-001     | RESERVED  | scooter | Model B | Excellent |
      | 3  | EQ-003       | HELM-ADULT-001 | AVAILABLE | helmet  | Model B | Excellent |
    Examples:
      | rentalId | customerId | plannedDuration |
      | 1        | CUS1       | 60              |

  Scenario Outline: Update rental - select equipment twice
    Given a single rental exists in the database with the following data
      | id         | customerId   | plannedDuration   | status | createdAt           | updatedAt           |
      | <rentalId> | <customerId> | <plannedDuration> | DRAFT  | 2026-02-06T10:00:00 | 2026-02-06T10:00:00 |
    And the rental update request is
      | op      | path          | value |
      | replace | /equipmentIds | [2]   |
    When a PATCH request has been made to "/api/rentals/{requestedObjectId}" endpoint with context
    Then the response status is 200
    And the rental response only contains
      | customerId   | status | plannedDuration   | estimatedCost |
      | <customerId> | DRAFT  | <plannedDuration> | 15.00         |
    And the rental response only contains rental equipments
      | equipmentId | equipmentUid | status   | estimatedCost | finalCost |
      | 2           | E-BIKE-001   | ASSIGNED | 15.00         |           |
    #    rental module
    And rental was persisted in database
      | customerId   | status | plannedDuration   |
      | <customerId> | DRAFT  | <plannedDuration> |
    And rental equipment was persisted in database
      | rentalId   | equipmentId | equipmentUid | status   | estimatedCost |
      | <rentalId> | 2           | E-BIKE-001   | ASSIGNED | 15.00         |
#    equipment module
    And the following equipment record was persisted in db
      | id | serialNumber | uid        | status   | type    | model   | condition |
      | 2  | EQ-002       | E-BIKE-001 | RESERVED | scooter | Model B | Excellent |
# second update equipments
    And the rental update request is
      | op      | path          | value |
      | replace | /equipmentIds | [2,1] |
    When a PATCH request has been made to "/api/rentals/{requestedObjectId}" endpoint with context
    Then the response status is 200
    And the rental response only contains
      | customerId   | status | plannedDuration   | estimatedCost |
      | <customerId> | DRAFT  | <plannedDuration> | 24.00         |
    And the rental response only contains rental equipments
      | equipmentId | equipmentUid | status   | estimatedCost | finalCost |
      | 1           | BIKE-001     | ASSIGNED | 9.00          |           |
      | 2           | E-BIKE-001   | ASSIGNED | 15.00         |           |
    Examples:
      | rentalId | customerId | plannedDuration |
      | 1        | CUS1       | 60              |

  Scenario Outline: Update rental - set duration
    Given a single rental exists in the database with the following data
      | id         | customerId   | status | createdAt           | updatedAt           |
      | <rentalId> | <customerId> | DRAFT  | 2026-02-06T10:00:00 | 2026-02-06T10:00:00 |
    And rental equipment exists in the database with the following data
      | rentalId   | equipmentId   | equipmentUid | equipmentType | tariffId | status   | startedAt           | expectedReturnAt    | estimatedCost | createdAt           | updatedAt           |
      | <rentalId> | <equipmentId> | BIKE-001     | bicycle       | 1        | ASSIGNED | 2026-02-10T08:00:00 | 2026-02-10T10:00:00 | 200.00        | 2026-02-10T08:00:00 | 2026-02-10T08:00:00 |
    And the rental update request is
      | op      | path      | value |
      | replace | /duration | PT3H  |
    When a PATCH request has been made to "/api/rentals/{requestedObjectId}" endpoint with context
    Then the response status is 200
    And the rental response only contains
      | customerId   | status | plannedDuration   | estimatedCost |
      | <customerId> | DRAFT  | <plannedDuration> | 200.00        |
    And the rental response only contains rental equipments
      | equipmentId   | equipmentUid | status   | tariffId | estimatedCost | finalCost |
      | <equipmentId> | BIKE-001     | ASSIGNED | 1        | 200.00        |           |
    And rental was persisted in database
      | customerId   | status | plannedDuration   |
      | <customerId> | DRAFT  | <plannedDuration> |
    Examples:
      | rentalId | equipmentId | customerId | plannedDuration |
      | 1        | 1           | CUS1       | 180             |

  Scenario Outline: Update rental - activate rental
    Given now is "<now>"
    And a single rental exists in the database with the following data
      | id         | customerId | status | plannedDuration | createdAt | updatedAt |
      | <rentalId> | <customer> | DRAFT  | 120             | <now>     | <now>     |
    And rental equipment exists in the database with the following data
      | rentalId   | equipmentId   | equipmentUid | equipmentType | tariffId   | status   | startedAt           | expectedReturnAt    | estimatedCost   | createdAt           |
      | <rentalId> | <equipmentId> | BIKE-001     | bicycle       | <tariffId> | ASSIGNED | 2026-02-10T08:00:00 | 2026-02-10T10:00:00 | <estimatedCost> | 2026-02-10T08:00:00 |
    And the following transaction records exist in db
      | id  | type | paymentMethod | amount          | customerId | operatorId | sourceType | sourceId   | recordedAt          | idempotencyKey |
      | TX2 | HOLD | CASH          | <estimatedCost> | <customer> | OP1        | RENTAL     | <rentalId> | 2026-02-10T08:00:00 | IDK2           |
    And the rental update request is
      | op      | path    | value  |
      | replace | /status | ACTIVE |
    When a PATCH request has been made to "/api/rentals/{rentalId}" endpoint with
      | {rentalId} |
      | <rentalId> |
    Then the response status is 200
    And the rental response only contains
      | customerId | status | estimatedCost   | plannedDuration   | startedAt |
      | <customer> | ACTIVE | <estimatedCost> | <plannedDuration> | <now>     |
    And the rental response only contains rental equipments
      | equipmentId   | equipmentUid | status | tariffId   | estimatedCost   | finalCost |
      | <equipmentId> | BIKE-001     | ACTIVE | <tariffId> | <estimatedCost> |           |
#    rental module
    And rental was persisted in database
      | customerId | status | createdAt | plannedDuration   |
      | <customer> | ACTIVE | <now>     | <plannedDuration> |
    And rental equipment was persisted in database
      | rentalId   | equipmentId   | equipmentUid | status | estimatedCost   | tariffId   |
      | <rentalId> | <equipmentId> | BIKE-001     | ACTIVE | <estimatedCost> | <tariffId> |
#    equipment module
    And the following rental started event was published
      | customerId | equipmentId   | startedAt |
      | <customer> | <equipmentId> | <now>     |
    And the following equipment record was persisted in db
      | id            | serialNumber | uid      | status | type    | model   | condition |
      | <equipmentId> | EQ-001       | BIKE-001 | RENTED | bicycle | Model A | Good      |
    Examples:
      | rentalId | equipmentId | tariffId | customer | now                 | plannedDuration | estimatedCost |
      | RENT2    | 1           | 1        | CUS2     | 2026-02-10T10:30:00 | 120             | 200           |

  Scenario Outline: Update rental - activate rental
    Given now is "<now>"
    And a single rental exists in the database with the following data
      | id         | customerId | status | plannedDuration | createdAt | updatedAt |
      | <rentalId> | <customer> | DRAFT  | 120             | <now>     | <now>     |
    And rental equipment exists in the database with the following data
      | rentalId   | equipmentId   | equipmentUid | equipmentType | tariffId   | status   | startedAt           | expectedReturnAt    | estimatedCost   | createdAt           |
      | <rentalId> | <equipmentId> | BIKE-001     | bicycle       | <tariffId> | ASSIGNED | 2026-02-10T08:00:00 | 2026-02-10T10:00:00 | <estimatedCost> | 2026-02-10T08:00:00 |
    And the following transaction records exist in db
      | id  | type | paymentMethod | amount          | customerId | operatorId | sourceType | sourceId   | recordedAt          | idempotencyKey |
      | TX2 | HOLD | CASH          | <estimatedCost> | <customer> | OP1        | RENTAL     | <rentalId> | 2026-02-10T08:00:00 | IDK2           |
    And the rental update request is
      | op      | path    | value  |
      | replace | /status | ACTIVE |
    When a PATCH request has been made to "/api/rentals/{rentalId}" endpoint with
      | {rentalId} |
      | <rentalId> |
    Then the response status is 200
    And the rental response only contains
      | customerId | status | estimatedCost   | plannedDuration   | startedAt |
      | <customer> | ACTIVE | <estimatedCost> | <plannedDuration> | <now>     |
    And the rental response only contains rental equipments
      | equipmentId   | equipmentUid | status | tariffId   | estimatedCost   | finalCost |
      | <equipmentId> | BIKE-001     | ACTIVE | <tariffId> | <estimatedCost> |           |
#    rental module
    And rental was persisted in database
      | customerId | status | createdAt | plannedDuration   |
      | <customer> | ACTIVE | <now>     | <plannedDuration> |
    And rental equipment was persisted in database
      | rentalId   | equipmentId   | equipmentUid | status | estimatedCost   | tariffId   |
      | <rentalId> | <equipmentId> | BIKE-001     | ACTIVE | <estimatedCost> | <tariffId> |
#    equipment module
    And the following rental started event was published
      | customerId | equipmentId   | startedAt |
      | <customer> | <equipmentId> | <now>     |
    And the following equipment record was persisted in db
      | id            | serialNumber | uid      | status | type    | model   | condition |
      | <equipmentId> | EQ-001       | BIKE-001 | RENTED | bicycle | Model A | Good      |
    Examples:
      | rentalId | equipmentId | tariffId | customer | now                 | plannedDuration | estimatedCost |
      | RENT2    | 1           | 1        | CUS2     | 2026-02-10T10:30:00 | 120             | 200           |

  Scenario: Attempt to activate rental without hold
    Given a single rental exists in the database with the following data
      | id | customerId | status | plannedDuration | createdAt           | updatedAt           |
      | 1  | CUS1       | DRAFT  | 120             | 2026-02-06T10:00:00 | 2026-02-06T10:00:00 |
    And rental equipment exists in the database with the following data
      | rentalId | equipmentId | equipmentUid | equipmentType | tariffId | status   | startedAt           | expectedReturnAt    | estimatedCost | createdAt           |
      | 1        | 1           | BIKE-001     | bicycle       | 1        | ASSIGNED | 2026-02-10T08:00:00 | 2026-02-10T10:00:00 | 200.00        | 2026-02-10T08:00:00 |
    And the rental update request is
      | op      | path    | value  |
      | replace | /status | ACTIVE |
    When a PATCH request has been made to "/api/rentals/{requestedObjectId}" endpoint with context
    Then the response status is 409
    And the response contains
      | path     | value                                                     |
      | $.title  | Hold required                                             |
      | $.detail | A fund hold must exist before the rental can be activated |

  Scenario: Update rental without duration. Duration must present
    Given a single rental exists in the database with the following data
      | id | customerId | equipmentId | tariffId | status | createdAt           | updatedAt           |
      | 1  | CUS1       | 1           | 2        | DRAFT  | 2026-02-06T10:00:00 | 2026-02-06T10:00:00 |
    And rental equipment exists in the database with the following data
      | rentalId | equipmentId | equipmentUid | equipmentType | tariffId | status   | startedAt           | expectedReturnAt    | estimatedCost | createdAt           |
      | 1        | 1           | BIKE-001     | bicycle       | 1        | ASSIGNED | 2026-02-10T08:00:00 | 2026-02-10T10:00:00 | 200.00        | 2026-02-10T08:00:00 |
    And the rental update request is
      | op      | path          | value |
      | replace | /customerId   | CUS2  |
      | replace | /equipmentIds | [2]   |
    When a PATCH request has been made to "/api/rentals/{requestedObjectId}" endpoint with context
    Then the response status is 422
    And the response contains
      | path        | value                                                     |
      | $.title     | Invalid rental planned duration                           |
      | $.detail    | Cannot perform operation on rental. Duration must present |
      | $.errorCode | rental.planned-duration.invalid                           |

  Scenario: Update rental - combined update
    Given a single rental exists in the database with the following data
      | id | customerId | equipmentId | tariffId | status | plannedDuration | createdAt           | updatedAt           |
      | 1  | CUS1       | 1           | 2        | DRAFT  | 60              | 2026-02-06T10:00:00 | 2026-02-06T10:00:00 |
    And rental equipment exists in the database with the following data
      | rentalId | equipmentId | equipmentUid | equipmentType | tariffId | status   | startedAt           | expectedReturnAt    | estimatedCost | createdAt           |
      | 1        | 1           | BIKE-001     | bicycle       | 1        | ASSIGNED | 2026-02-10T08:00:00 | 2026-02-10T10:00:00 | 200.00        | 2026-02-10T08:00:00 |
    And the rental update request is
      | op      | path          | value |
      | replace | /customerId   | CUS2  |
      | replace | /equipmentIds | [2]   |
    When a PATCH request has been made to "/api/rentals/{requestedObjectId}" endpoint with context
    Then the response status is 200
    And the rental response only contains
      | customerId | status | plannedDuration |
      | CUS2       | DRAFT  | 60              |
    And the rental response only contains rental equipments
      | equipmentId | equipmentUid | status   | tariffId | estimatedCost | finalCost |
      | 2           | E-BIKE-001   | ASSIGNED |          | 15.00         |           |

  # Rental Query Scenarios
  Scenario Outline: Get rental by ID
    Given a single rental exists in the database with the following data
      | id         | customerId   | equipmentId   | status   | plannedDuration   | createdAt           | updatedAt           |
      | <rentalId> | <customerId> | <equipmentId> | <status> | <plannedDuration> | 2026-02-06T10:00:00 | 2026-02-06T10:00:00 |
    And rental equipment exists in the database with the following data
      | rentalId   | equipmentId   | equipmentUid   | equipmentType | tariffId   | status   | startedAt           | expectedReturnAt    | estimatedCost | createdAt           |
      | <rentalId> | <equipmentId> | BIKE-001       | bicycle       | <tariffId> | ASSIGNED | 2026-02-10T08:00:00 | 2026-02-10T10:00:00 | 200.00        | 2026-02-10T08:00:00 |
      | <rentalId> | 3             | HELM-ADULT-001 | helm          | 3          | ASSIGNED | 2026-02-10T08:00:00 | 2026-02-10T10:00:00 | 10.00         | 2026-02-10T08:00:00 |
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

  Scenario: Create rental holds funds from customer wallet — sufficient balance
    Given a rental request with the following data
      | customerId | equipmentIds | duration | operatorId |
      | CUS1       | 1,3          | PT2H     | OP1        |
    When a POST request has been made to "/api/rentals" endpoint
    Then the response status is 201
    And the following sub-ledger records were persisted in db
      | id     | accountId | ledgerType      | balance |
      | L_C_W1 | ACC1      | CUSTOMER_WALLET | 283.00  |
      | L_C_H1 | ACC1      | CUSTOMER_HOLD   | 17.00   |
    And the following transactions were persisted in db
      | customerId | amount | type | paymentMethod     | operatorId |
      | CUS1       | 17.00  | HOLD | INTERNAL_TRANSFER | OP1        |

  Scenario: Create rental rejected when customer wallet has insufficient balance
    Given a rental request with the following data
      | customerId | equipmentIds | duration | operatorId |
      | CUS2       | 1,3          | PT2H     | OP1        |
    When a POST request has been made to "/api/rentals" endpoint
    Then the response status is 422
    And there are only 2 transactions in db
    And the response contains
      | path        | value                     |
      | $.errorCode | rental.insufficient_funds |

  Scenario: Create rental with discount — hold reflects discounted total
    Given a rental request with the following data
      | customerId | equipmentIds | duration | operatorId | discountPercent |
      | CUS1       | 1            | PT2H     | OP1        | 10              |
    When a POST request has been made to "/api/rentals" endpoint
    Then the response status is 201
    And the rental response only contains
      | customerId | status | plannedDuration | estimatedCost |
      | CUS1       | DRAFT  | 120             | 14.40         |
    And the rental response only contains rental equipments
      | equipmentId | equipmentUid | status   | estimatedCost |
      | 1           | BIKE-001     | ASSIGNED | 16.00         |
    And rental was persisted in database
      | estimatedCost | discountPercent |
      | 14.40         | 10              |
    And rental equipments were persisted in database
      | equipmentId | equipmentUid | status   | estimatedCost |
      | 1           | BIKE-001     | ASSIGNED | 16.00         |

  Scenario: Create rental with SPECIAL tariff — specialPrice used as total
    Given a rental request with the following data
      | customerId | equipmentIds | duration | operatorId | specialTariffId | specialPrice |
      | CUS1       | 1            | PT2H     | OP1        | 13              | 15.00        |
    When a POST request has been made to "/api/rentals" endpoint
    Then the response status is 201
    And the rental response only contains
      | customerId | status | plannedDuration | estimatedCost |
      | CUS1       | DRAFT  | 120             | 15.00         |
    And the rental response only contains rental equipments
      | equipmentId | equipmentUid | status   | estimatedCost |
      | 1           | BIKE-001     | ASSIGNED | 0.00          |
    And rental was persisted in database
      | specialTariffId | specialPrice |
      | 13              | 15.00        |
    And rental equipments were persisted in database
      | equipmentId | equipmentUid | status   | estimatedCost |
      | 1           | BIKE-001     | ASSIGNED | 0.00          |

  Scenario: Create rental with SPECIAL tariff — ZERO specialPrice used as total
    Given a rental request with the following data
      | customerId | equipmentIds | duration | operatorId | specialTariffId | specialPrice |
      | CUS1       | 1            | PT2H     | OP1        | 13              | 0.00         |
    When a POST request has been made to "/api/rentals" endpoint
    Then the response status is 201
    And the rental response only contains
      | customerId | status | plannedDuration | estimatedCost |
      | CUS1       | DRAFT  | 120             | 0.00          |
    And the rental response only contains rental equipments
      | equipmentId | equipmentUid | status   | estimatedCost |
      | 1           | BIKE-001     | ASSIGNED | 0.00          |
    And rental was persisted in database
      | specialTariffId | specialPrice |
      | 13              | 0.00         |
    And rental equipments were persisted in database
      | equipmentId | equipmentUid | status   | estimatedCost |
      | 1           | BIKE-001     | ASSIGNED | 0.00          |

  Scenario: Rejected — specialTariffId references a non-SPECIAL tariff type
    Given a rental request with the following data
      | customerId | equipmentIds | duration | operatorId | specialTariffId | specialPrice |
      | CUS1       | 1            | PT2H     | OP1        | 10              | 15.00        |
    When a POST request has been made to "/api/rentals" endpoint
    Then the response status is 422
    And the response contains
      | path        | value                       |
      | $.errorCode | tariff.special.type_invalid |