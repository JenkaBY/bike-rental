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
      | slug        | name        | description       | transitions               |
      | BROKEN      | Broken      | Not Ready to rent | AVAILABLE,MAINTENANCE     |
      | AVAILABLE   | Available   | Ready to rent     | BROKEN,MAINTENANCE,RENTED |
      | MAINTENANCE | Maintenance | null              | AVAILABLE                 |
      | RENTED      | Rented      | In use already    | AVAILABLE,BROKEN          |
    And the following equipment types exist in the database
      | slug    | name    | description |
      | bicycle | Bicycle | Two-wheeled |
      | scooter | Scooter | Scooter     |
    And the following equipment records exist in db
      | id | serialNumber | uid        | status    | type    | model   | condition |
      | 1  | EQ-001       | BIKE-001   | AVAILABLE | bicycle | Model A | Good      |
      | 2  | EQ-002       | E-BIKE-001 | AVAILABLE | scooter | Model B | Excellent |
    And the following tariff records exist in db
      | id | name        | description     | equipmentType | basePrice | halfHourPrice | hourPrice | dayPrice | discountedPrice | validFrom  | validTo    | status |
      | 1  | Hourly Rate | Standard hourly | bicycle       | 100.00    | 60.00         | 100.00    | 500.00   | 90.00           | 2026-01-01 | 2026-12-31 | ACTIVE |
      | 2  | Daily Rate  | Standard daily  | bicycle       | 200.00    | 70.00         | 110.00    | 600.00   | 95.00           | 2026-01-01 | 2026-12-31 | ACTIVE |

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

  Scenario Outline: Create rental with all required fields
    Given a rental request with the following data
      | customerId   | equipmentId   | duration   | tariffId   |
      | <customerId> | <equipmentId> | <duration> | <tariffId> |
    When a POST request has been made to "/api/rentals" endpoint
    Then the response status is 201
    And the rental response only contains
      | customerId   | equipmentId   | status | plannedDuration   | tariffId   | estimatedCost   |
      | <customerId> | <equipmentId> | DRAFT  | <plannedDuration> | <tariffId> | <estimatedCost> |
    And the following rental created event was published
      | customerId   | status |
      | <customerId> | DRAFT  |
    Examples:
      | customerId | equipmentId | duration | tariffId | plannedDuration | estimatedCost |
      | CUS1       | 1           | PT2H     | 2        | 120             | 200.00        |


  Scenario: Create rental with auto-selected tariff
    Given a rental request with the following data
      | customerId | equipmentId | duration | tariffId |
      | CUS1       | 1           | PT2H     |          |
    When a POST request has been made to "/api/rentals" endpoint
    Then the response status is 201
    And the rental response only contains
      | customerId | equipmentId | status | tariffId | estimatedCost | plannedDuration |
      | CUS1       | 1           | DRAFT  | 1        | 100.0         | 120             |
    And the following rental created event was published
      | customerId | status |
      | CUS1       | DRAFT  |

  @ResetClock
  Scenario: Create rental with auto-selected tariff when no suitable tariff found
    Given today is "2026-02-09"
    And the following equipment records exist in db
      | id | serialNumber | uid      | status    | type    | model   | condition |
      | 4  | EQ-004       | BIKE-004 | AVAILABLE | scooter | Model D | Good      |
    And a rental request with the following data
      | customerId | equipmentId | duration | tariffId |
      | CUS1       | 4           | PT2H     |          |
    When a POST request has been made to "/api/rentals" endpoint
    Then the response status is 404
    And the response contains
      | path     | value                                                                                              |
      | $.title  | Suitable tariff not found                                                                          |
      | $.detail | No suitable tariff found for equipment type 'scooter' on date 2026-02-09 for duration: 120 minutes |

  # Rental Update Scenarios (JSON Patch)

  Scenario: Update rental - select customer
    Given a single rental exists in the database with the following data
      | customerId | equipmentId | tariffId | status | createdAt            | updatedAt            |
      | CUS1       | 1           | 1        | DRAFT  | 2026-02-06T10:00:00Z | 2026-02-06T10:00:00Z |
    And the rental update request is
      | op      | path        | value |
      | replace | /customerId | CUS2  |
    When a PATCH request has been made to "/api/rentals/{requestedObjectId}" endpoint with context
    Then the response status is 200
    And the rental response only contains
      | customerId | equipmentId | tariffId | status |
      | CUS2       | 1           | 1        | DRAFT  |

  Scenario: Update rental - select equipment
    Given a single rental exists in the database with the following data
      | customerId | equipmentId | tariffId | status | createdAt            | updatedAt            |
      | CUS1       | 1           | 1        | DRAFT  | 2026-02-06T10:00:00Z | 2026-02-06T10:00:00Z |
    And the rental update request is
      | op      | path         | value |
      | replace | /equipmentId | 2     |
    When a PATCH request has been made to "/api/rentals/{requestedObjectId}" endpoint with context
    Then the response status is 200
    And the rental response only contains
      | customerId | equipmentId | tariffId | status |
      | CUS1       | 2           | 1        | DRAFT  |

  Scenario: Update rental - set duration
    Given a single rental exists in the database with the following data
      | customerId | equipmentId | tariffId | status | createdAt            | updatedAt            |
      | CUS1       | 1           | 1        | DRAFT  | 2026-02-06T10:00:00Z | 2026-02-06T10:00:00Z |
    And the rental update request is
      | op      | path      | value |
      | replace | /duration | PT3H  |
    When a PATCH request has been made to "/api/rentals/{requestedObjectId}" endpoint with context
    Then the response status is 200
    And the rental response only contains
      | customerId | equipmentId | tariffId | status | plannedDuration | cost |
      | CUS1       | 1           | 1        | DRAFT  | 180             | 100  |

  Scenario Outline: Record prepayment for draft rental
    Given now is "<now>"
    And a single rental exists in the database with the following data
      | id         | customerId | equipmentId | tariffId | status | estimatedCost | plannedDuration | createdAt | updatedAt |
      | <rentalId> | CUS1       | 1           | 1        | DRAFT  | 100.00        | 120             | <now>Z    | <now>Z    |
    And the prepayment request is
      | amount   | method   | operator |
      | <amount> | <method> | OP1      |
    When a POST request has been made to "/api/rentals/<rentalId>/prepayments" endpoint
    Then the response status is 201
    And the prepayment response contains
      | amount   | paymentMethod | createdAt |
      | <amount> | <method>      | <now>Z    |
    And the following payment received event was published
      | rentalId   | amount   | type       | receivedAt |
      | <rentalId> | <amount> | PREPAYMENT | <now>Z     |
    Examples:
      | rentalId | amount | method | now                 |
      | 1        | 100.00 | CASH   | 2026-02-10T10:15:30 |

  Scenario: Reject prepayment when amount is below estimated cost
    Given a single rental exists in the database with the following data
      | customerId | equipmentId | tariffId | status | estimatedCost | plannedDuration | createdAt            | updatedAt            |
      | CUS1       | 1           | 1        | DRAFT  | 100.00        | 120             | 2026-02-06T10:00:00Z | 2026-02-06T10:00:00Z |
    And the prepayment request is
      | amount | method | operator |
      | 50.00  | CASH   | OP1      |
    When a POST request has been made to "/api/rentals/{requestedObjectId}/prepayments" endpoint with context
    Then the response status is 422
    And the response contains
      | path     | value                                                               |
      | $.title  | Insufficient prepayment                                             |
      | $.detail | Prepayment amount must be at least the estimated cost of the rental |

  Scenario Outline: Update rental - activate rental
    Given now is "<now>"
    And a single rental exists in the database with the following data
      | id         | customerId | equipmentId   | tariffId   | status | estimatedCost | plannedDuration | createdAt | updatedAt |
      | <rentalId> | <customer> | <equipmentId> | <tariffId> | DRAFT  | 100.00        | 120             | <now>Z    | <now>Z    |
    And the prepayment request is
      | amount | method | operator |
      | 100.00 | CASH   | OP1      |
    When a POST request has been made to "/api/rentals/<rentalId>/prepayments" endpoint
    Then the response status is 201
    And the prepayment response contains
      | amount             | paymentMethod | createdAt |
      | <prepaymentAmount> | CASH          | <now>Z    |
    And the following payment received event was published
      | rentalId   | amount             | type       | receivedAt |
      | <rentalId> | <prepaymentAmount> | PREPAYMENT | <now>Z     |
    And the rental update request is
      | op      | path    | value  |
      | replace | /status | ACTIVE |
    When a PATCH request has been made to "/api/rentals/<rentalId>" endpoint
    Then the response status is 200
    And the rental response only contains
      | customerId | equipmentId   | tariffId   | status | estimatedCost | plannedDuration | startedAt |
      | <customer> | <equipmentId> | <tariffId> | ACTIVE | 100.00        | 120             | <now>Z    |
    And the following rental started event was published
      | customerId | equipmentId   | startedAt |
      | <customer> | <equipmentId> | <now>Z    |
    And the following equipment record was persisted in db
      | id            | serialNumber | uid      | status | type    | model   | condition |
      | <equipmentId> | EQ-001       | BIKE-001 | RENTED | bicycle | Model A | Good      |
    Examples:
      | rentalId | equipmentId | tariffId | customer | now                 | prepaymentAmount |
      | 5        | 1           | 1        | CUS1     | 2026-02-10T10:30:00 | 100.00           |

  Scenario: Attempt to activate rental without prepayment
    Given a single rental exists in the database with the following data
      | customerId | equipmentId | tariffId | status | estimatedCost | plannedDuration | createdAt            | updatedAt            |
      | CUS1       | 1           | 1        | DRAFT  | 100.00        | 120             | 2026-02-06T10:00:00Z | 2026-02-06T10:00:00Z |
    And the rental update request is
      | op      | path    | value  |
      | replace | /status | ACTIVE |
    When a PATCH request has been made to "/api/rentals/{requestedObjectId}" endpoint with context
    Then the response status is 422
    And the response contains
      | path     | value                                              |
      | $.title  | Prepayment required                                |
      | $.detail | Prepayment must be received before starting rental |

  Scenario: Update rental - combined update
    Given a single rental exists in the database with the following data
      | customerId | equipmentId | tariffId | status | createdAt            | updatedAt            |
      | CUS1       | 1           | 2        | DRAFT  | 2026-02-06T10:00:00Z | 2026-02-06T10:00:00Z |
    And the rental update request is
      | op      | path         | value |
      | replace | /customerId  | CUS2  |
      | replace | /equipmentId | 2     |
    When a PATCH request has been made to "/api/rentals/{requestedObjectId}" endpoint with context
    Then the response status is 200
    And the rental response only contains
      | customerId | equipmentId | tariffId | status |
      | CUS2       | 2           | 2        | DRAFT  |

  # Rental Query Scenarios

  Scenario Outline: Get rental by ID
    Given a single rental exists in the database with the following data
      | customerId   | equipmentId   | tariffId   | status   | createdAt            | updatedAt            |
      | <customerId> | <equipmentId> | <tariffId> | <status> | 2026-02-06T10:00:00Z | 2026-02-06T10:00:00Z |
    When a GET request has been made to "/api/rentals/{requestedObjectId}" endpoint with context
    Then the response status is 200
    And the rental response only contains
      | customerId   | equipmentId   | tariffId   | status   |
      | <customerId> | <equipmentId> | <tariffId> | <status> |
    Examples:
      | customerId | equipmentId | tariffId | status |
      | CUS1       | 1           | 1        | DRAFT  |

  Scenario: Get rental by non-existent ID
    When a GET request has been made to "/api/rentals/{id}" endpoint with
      | {id} |
      | 999  |
    Then the response status is 404
    And the response contains
      | path     | value                                  |
      | $.title  | Not Found                              |
      | $.detail | Rental with identifier '999' not found |
