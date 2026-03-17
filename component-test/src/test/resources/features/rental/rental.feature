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

  # Rental Creation Scenarios

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
      | customerId   | equipmentIds                 | duration   |
      | <customerId> | <equipmentId>,<equipmentId2> | <duration> |
    When a POST request has been made to "/api/rentals" endpoint
    Then the response status is 201
    And the rental response only contains
      | customerId   | status | plannedDuration   | estimatedCost |
      | <customerId> | DRAFT  | <plannedDuration> | 220.00        |
    And the rental response only contains rental equipments
      | equipmentId    | equipmentUid   | status   | tariffId | estimatedCost | finalCost |
      | <equipmentId>  | BIKE-001       | ASSIGNED | 1        | 200.00        |           |
      | <equipmentId2> | HELM-ADULT-001 | ASSIGNED | 3        | 20.00         |           |
    #    rental module
    And rental equipments were persisted in database
      | equipmentId    | equipmentUid   | tariffId | status   | estimatedCost |
      | <equipmentId>  | BIKE-001       | 1        | ASSIGNED | 200.00        |
      | <equipmentId2> | HELM-ADULT-001 | 3        | ASSIGNED | 20.00         |
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
      | customerId | equipmentIds | duration | tariffId |
      | CUS1       | 4            | PT2H     |          |
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
      | rentalId   | equipmentId | equipmentUid   | tariffId | status   | startedAt           | expectedReturnAt    | estimatedCost | createdAt           | updatedAt           |
      | <rentalId> | 1           | BIKE-001       | 1        | ASSIGNED | 2026-02-10T08:00:00 | 2026-02-10T10:00:00 | 200.00        | 2026-02-10T08:00:00 | 2026-02-10T08:00:00 |
      | <rentalId> | 3           | HELM-ADULT-001 | 3        | ASSIGNED | 2026-02-10T08:00:00 | 2026-02-10T10:00:00 | 20.00         | 2026-02-10T08:00:00 | 2026-02-10T08:00:00 |
    And the rental update request is
      | op      | path          | value |
      | replace | /equipmentIds | [2]   |
    When a PATCH request has been made to "/api/rentals/{requestedObjectId}" endpoint with context
    Then the response status is 200
    And the rental response only contains
      | customerId   | status | plannedDuration   | estimatedCost |
      | <customerId> | DRAFT  | <plannedDuration> | 300.00        |
    And the rental response only contains rental equipments
      | equipmentId | equipmentUid | status   | tariffId | estimatedCost | finalCost |
      | 2           | E-BIKE-001   | ASSIGNED | 4        | 300.00        |           |
    And the following equipment records were persisted in db
      | id | serialNumber | uid            | status    | type    | model   | condition |
      | 1  | EQ-001       | BIKE-001       | AVAILABLE | bicycle | Model A | Good      |
      | 2  | EQ-002       | E-BIKE-001     | RESERVED  | scooter | Model B | Excellent |
      | 3  | EQ-003       | HELM-ADULT-001 | AVAILABLE | helmet  | Model B | Excellent |
    Examples:
      | rentalId | customerId | plannedDuration |
      | 1        | CUS1       | 60              |

  Scenario Outline: Update rental - set duration
    Given a single rental exists in the database with the following data
      | id         | customerId   | status | createdAt           | updatedAt           |
      | <rentalId> | <customerId> | DRAFT  | 2026-02-06T10:00:00 | 2026-02-06T10:00:00 |
    And rental equipment exists in the database with the following data
      | rentalId   | equipmentId   | equipmentUid | tariffId | status   | startedAt           | expectedReturnAt    | estimatedCost | createdAt           | updatedAt           |
      | <rentalId> | <equipmentId> | BIKE-001     | 1        | ASSIGNED | 2026-02-10T08:00:00 | 2026-02-10T10:00:00 | 200.00        | 2026-02-10T08:00:00 | 2026-02-10T08:00:00 |
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

  Scenario Outline: Record prepayment for draft rental
    Given now is "<now>"
    And a single rental exists in the database with the following data
      | id         | customerId | status | plannedDuration | createdAt | updatedAt |
      | <rentalId> | CUS1       | DRAFT  | 120             | <now>     | <now>     |
    And rental equipment exists in the database with the following data
      | rentalId   | equipmentId   | equipmentUid | tariffId | status   | startedAt           | expectedReturnAt    | estimatedCost | createdAt           | updatedAt           |
      | <rentalId> | <equipmentId> | BIKE-001     | 1        | ASSIGNED | 2026-02-10T08:00:00 | 2026-02-10T10:00:00 | 200.00        | 2026-02-10T08:00:00 | 2026-02-10T08:00:00 |
    And the prepayment request is
      | amount   | method   | operator |
      | <amount> | <method> | OP1      |
    When a POST request has been made to "/api/rentals/<rentalId>/prepayments" endpoint
    Then the response status is 201
    And the prepayment response contains
      | amount   | paymentMethod | createdAt |
      | <amount> | <method>      | <now>     |
    And the following payment received event was published
      | rentalId   | amount   | type       | receivedAt |
      | <rentalId> | <amount> | PREPAYMENT | <now>      |
    Examples:
      | rentalId | equipmentId | amount | method | now                 |
      | 10       | 1           | 200.00 | CASH   | 2026-02-10T10:15:30 |

  Scenario Outline: Reject prepayment when amount is below estimated cost
    Given a single rental exists in the database with the following data
      | id         | customerId | status | plannedDuration | createdAt           | updatedAt           |
      | <rentalId> | CUS1       | DRAFT  | 120             | 2026-02-06T10:00:00 | 2026-02-06T10:00:00 |
    And rental equipment exists in the database with the following data
      | rentalId   | equipmentId   | equipmentUid | tariffId | status   | startedAt           | expectedReturnAt    | estimatedCost | createdAt           | updatedAt           |
      | <rentalId> | <equipmentId> | BIKE-001     | 1        | ASSIGNED | 2026-02-10T08:00:00 | 2026-02-10T10:00:00 | 200.00        | 2026-02-10T08:00:00 | 2026-02-10T08:00:00 |
    And the prepayment request is
      | amount | method | operator |
      | 50.00  | CASH   | OP1      |
    When a POST request has been made to "/api/rentals/{requestedObjectId}/prepayments" endpoint with context
    Then the response status is 422
    And the response contains
      | path     | value                                                               |
      | $.title  | Insufficient prepayment                                             |
      | $.detail | Prepayment amount must be at least the estimated cost of the rental |
    Examples:
      | rentalId | equipmentId |
      | 10       | 1           |

  Scenario Outline: Update rental - activate rental
    Given now is "<now>"
    And a single rental exists in the database with the following data
      | id         | customerId | status | plannedDuration | createdAt | updatedAt |
      | <rentalId> | <customer> | DRAFT  | 120             | <now>     | <now>     |
    And rental equipment exists in the database with the following data
      | rentalId   | equipmentId   | equipmentUid | tariffId | status   | startedAt           | expectedReturnAt    | estimatedCost | createdAt           |
      | <rentalId> | <equipmentId> | BIKE-001     | 1        | ASSIGNED | 2026-02-10T08:00:00 | 2026-02-10T10:00:00 | 200.00        | 2026-02-10T08:00:00 |
    And the prepayment request is
      | amount | method | operator |
      | 200.00 | CASH   | OP1      |
    When a POST request has been made to "/api/rentals/<rentalId>/prepayments" endpoint
    Then the response status is 201
    And the prepayment response contains
      | amount             | paymentMethod | createdAt |
      | <prepaymentAmount> | CASH          | <now>     |
    And the following payment received event was published
      | rentalId   | amount             | type       | receivedAt |
      | <rentalId> | <prepaymentAmount> | PREPAYMENT | <now>      |
    And the rental update request is
      | op      | path    | value  |
      | replace | /status | ACTIVE |
    When a PATCH request has been made to "/api/rentals/<rentalId>" endpoint
    Then the response status is 200
    And the rental response only contains
      | customerId | status | estimatedCost | plannedDuration   | startedAt |
      | <customer> | ACTIVE | 200.00        | <plannedDuration> | <now>     |
    And the rental response only contains rental equipments
      | equipmentId   | equipmentUid | status | tariffId   | estimatedCost | finalCost |
      | <equipmentId> | BIKE-001     | ACTIVE | <tariffId> | 200.00        |           |
#    rental module
    And rental was persisted in database
      | customerId | status | createdAt | plannedDuration   |
      | <customer> | ACTIVE | <now>     | <plannedDuration> |
    And the following equipment records were persisted in db
      | id            | serialNumber | uid      | status | type    | model   | condition |
      | <equipmentId> | EQ-001       | BIKE-001 | RENTED | bicycle | Model A | Good      |
#    equipment module
    And the following rental started event was published
      | customerId | equipmentId   | startedAt |
      | <customer> | <equipmentId> | <now>     |
    And the following equipment record was persisted in db
      | id            | serialNumber | uid      | status | type    | model   | condition |
      | <equipmentId> | EQ-001       | BIKE-001 | RENTED | bicycle | Model A | Good      |
    Examples:
      | rentalId | equipmentId | tariffId | customer | now                 | prepaymentAmount | plannedDuration |
      | 5        | 1           | 1        | CUS1     | 2026-02-10T10:30:00 | 200.00           | 120             |

  Scenario: Attempt to activate rental without prepayment
    Given a single rental exists in the database with the following data
      | id | customerId | status | plannedDuration | createdAt           | updatedAt           |
      | 1  | CUS1       | DRAFT  | 120             | 2026-02-06T10:00:00 | 2026-02-06T10:00:00 |
    And rental equipment exists in the database with the following data
      | rentalId | equipmentId | equipmentUid | tariffId | status   | startedAt           | expectedReturnAt    | estimatedCost | createdAt           |
      | 1        | 1           | BIKE-001     | 1        | ASSIGNED | 2026-02-10T08:00:00 | 2026-02-10T10:00:00 | 200.00        | 2026-02-10T08:00:00 |
    And the rental update request is
      | op      | path    | value  |
      | replace | /status | ACTIVE |
    When a PATCH request has been made to "/api/rentals/{requestedObjectId}" endpoint with context
    Then the response status is 422
    And the response contains
      | path     | value                                              |
      | $.title  | Prepayment required                                |
      | $.detail | Prepayment must be received before starting rental |

  Scenario: Update rental without duration. Duration must present
    Given a single rental exists in the database with the following data
      | id | customerId | equipmentId | tariffId | status | createdAt           | updatedAt           |
      | 1  | CUS1       | 1           | 2        | DRAFT  | 2026-02-06T10:00:00 | 2026-02-06T10:00:00 |
    And rental equipment exists in the database with the following data
      | rentalId | equipmentId | equipmentUid | tariffId | status   | startedAt           | expectedReturnAt    | estimatedCost | createdAt           |
      | 1        | 1           | BIKE-001     | 1        | ASSIGNED | 2026-02-10T08:00:00 | 2026-02-10T10:00:00 | 200.00        | 2026-02-10T08:00:00 |
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
      | rentalId | equipmentId | equipmentUid | tariffId | status   | startedAt           | expectedReturnAt    | estimatedCost | createdAt           |
      | 1        | 1           | BIKE-001     | 1        | ASSIGNED | 2026-02-10T08:00:00 | 2026-02-10T10:00:00 | 200.00        | 2026-02-10T08:00:00 |
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
      | 2           | E-BIKE-001   | ASSIGNED | 4        | 300.00        |           |

  # Rental Query Scenarios
  Scenario Outline: Get rental by ID
    Given a single rental exists in the database with the following data
      | id         | customerId   | equipmentId   | status   | plannedDuration   | createdAt           | updatedAt           |
      | <rentalId> | <customerId> | <equipmentId> | <status> | <plannedDuration> | 2026-02-06T10:00:00 | 2026-02-06T10:00:00 |
    And rental equipment exists in the database with the following data
      | rentalId   | equipmentId   | equipmentUid   | tariffId   | status   | startedAt           | expectedReturnAt    | estimatedCost | createdAt           |
      | <rentalId> | <equipmentId> | BIKE-001       | <tariffId> | ASSIGNED | 2026-02-10T08:00:00 | 2026-02-10T10:00:00 | 200.00        | 2026-02-10T08:00:00 |
      | <rentalId> | 3             | HELM-ADULT-001 | 3          | ASSIGNED | 2026-02-10T08:00:00 | 2026-02-10T10:00:00 | 10.00         | 2026-02-10T08:00:00 |
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
