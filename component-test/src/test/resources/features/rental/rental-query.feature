@ResetClock
Feature: Rental Query
  As an operator
  I want to query rentals with filtering and pagination
  So that I can view active rentals and track overdue rentals

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
      | BICYCLE | Bicycle | Two-wheeled |
      | SCOOTER | Scooter | Scooter     |
    And the following equipment records exist in db
      | id | serialNumber | uid        | status    | type    | model   | condition |
      | 1  | EQ-001       | BIKE-001   | AVAILABLE | BICYCLE | Model A | Good      |
      | 2  | EQ-002       | E-BIKE-001 | AVAILABLE | SCOOTER | Model B | Excellent |
      | 3  | EQ-003       | E-BIKE-002 | AVAILABLE | SCOOTER | Model B | Excellent |
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

  Scenario Outline: Get active rentals with status filter
    Given now is "<now>"
    And rentals exist in the database with the following data
      | id | customerId | equipmentId | equipmentUid | status    | startedAt   | expectedReturnAt    | createdAt   | updatedAt   |
      | 1  | CUS1       | 1           | BIKE-001     | ACTIVE    | <startedAt> | <expectedReturnAt>  | <startedAt> | <startedAt> |
      | 2  | CUS2       | 2           | E-BIKE-001   | ACTIVE    | <startedAt> | <expectedReturnAt2> | <startedAt> | <startedAt> |
      | 3  | CUS1       | 1           | BIKE-001     | DRAFT     | null        | null                | <startedAt> | <startedAt> |
      | 4  | CUS2       | 2           | E-BIKE-001   | COMPLETED | <startedAt> | <expectedReturnAt>  | <startedAt> | <startedAt> |
    And rental equipment exists in the database with the following data
      | rentalId | equipmentId | equipmentUid | equipmentType | tariffId | status   | startedAt   | expectedReturnAt   | estimatedCost | finalCost | createdAt   |
      | 1        | 1           | BIKE-001     | BICYCLE       | 1        | ACTIVE   | <startedAt> | <expectedReturnAt> | 200.00        |           | <startedAt> |
      | 2        | 2           | E-BIKE-001   | SCOOTER       | 2        | ACTIVE   | <startedAt> | <expectedReturnAt> | 200.00        |           | <startedAt> |
      | 3        | 1           | BIKE-001     | BICYCLE       | 1        | ASSIGNED |             |                    |               |           | <startedAt> |
      | 4        | 2           | E-BIKE-001   | SCOOTER       | 2        | RETURNED | <startedAt> | <expectedReturnAt> | 200.00        | 200.00    | <startedAt> |
    When a GET request has been made to "/api/rentals" endpoint with query parameters
      | status |
      | ACTIVE |
    Then the response status is 200
    And the rental summary response only contains page of
      | id | customerId | equipmentIds | status | startedAt   | expectedReturnAt    | overdueMin |
      | 1  | CUS1       | 1            | ACTIVE | <startedAt> | <expectedReturnAt>  | 43         |
      | 2  | CUS2       | 2            | ACTIVE | <startedAt> | <expectedReturnAt2> | 0          |
    And the response contains
      | path                 | value            |
      | $.totalItems         | 2                |
      | $.pageRequest.size   | 20               |
      | $.pageRequest.page   | 0                |
      | $.pageRequest.sortBy | expectedReturnAt |
    Examples:
      | now                 | startedAt           | expectedReturnAt    | expectedReturnAt2   |
      | 2026-02-18T11:43:00 | 2026-02-18T09:00:00 | 2026-02-18T11:00:00 | 2026-02-18T12:00:00 |

  Scenario Outline: Get active rentals filtered by customerId
    Given now is "<now>"
    And rentals exist in the database with the following data
      | id | customerId | status | startedAt   | expectedReturnAt   | createdAt   | updatedAt   |
      | 1  | CUS1       | ACTIVE | <startedAt> | <expectedReturnAt> | <startedAt> | <startedAt> |
      | 2  | CUS2       | ACTIVE | <startedAt> | <expectedReturnAt> | <startedAt> | <startedAt> |
      | 3  | CUS1       | ACTIVE | <startedAt> | <expectedReturnAt> | <startedAt> | <startedAt> |
    And rental equipment exists in the database with the following data
      | rentalId | equipmentId | equipmentUid | equipmentType | tariffId | status | startedAt   | expectedReturnAt   | estimatedCost | finalCost | createdAt   |
      | 1        | 1           | BIKE-001     | BICYCLE       | 1        | ACTIVE | <startedAt> | <expectedReturnAt> | 200.00        |           | <startedAt> |
      | 2        | 2           | E-BIKE-001   | SCOOTER       | 2        | ACTIVE | <startedAt> | <expectedReturnAt> | 200.00        |           | <startedAt> |
      | 3        | 3           | E-BIKE-002   | SCOOTER       | 2        | ACTIVE | <startedAt> | <expectedReturnAt> | 200.00        |           | <startedAt> |
    When a GET request has been made to "/api/rentals" endpoint with query parameters
      | status | customerId |
      | ACTIVE | CUS1       |
    Then the response status is 200
    And the rental summary response only contains page of
      | id | customerId | equipmentIds | status | startedAt   | expectedReturnAt   | overdueMin |
      | 1  | CUS1       | 1            | ACTIVE | <startedAt> | <expectedReturnAt> | 60         |
      | 3  | CUS1       | 3            | ACTIVE | <startedAt> | <expectedReturnAt> | 60         |
    And the response contains
      | path                 | value            |
      | $.totalItems         | 2                |
      | $.pageRequest.size   | 20               |
      | $.pageRequest.page   | 0                |
      | $.pageRequest.sortBy | expectedReturnAt |
    Examples:
      | now                 | startedAt           | expectedReturnAt    |
      | 2026-02-18T12:00:00 | 2026-02-18T09:00:00 | 2026-02-18T11:00:00 |

  Scenario Outline: Get active rentals with overdue calculation
    Given now is "<now>"
    And rentals exist in the database with the following data
      | id | customerId | status | startedAt   | expectedReturnAt       | createdAt   | updatedAt   |
      | 1  | CUS1       | ACTIVE | <startedAt> | <overdueExpectedAt>    | <startedAt> | <startedAt> |
      | 2  | CUS2       | ACTIVE | <startedAt> | <notOverdueExpectedAt> | <startedAt> | <startedAt> |
    And rental equipment exists in the database with the following data
      | rentalId | equipmentId | equipmentUid | equipmentType | tariffId | status | startedAt   | expectedReturnAt       | estimatedCost | finalCost | createdAt   |
      | 1        | 1           | BIKE-001     | BICYCLE       | 1        | ACTIVE | <startedAt> | <overdueExpectedAt>    | 200.00        |           | <startedAt> |
      | 2        | 2           | E-BIKE-001   | SCOOTER       | 2        | ACTIVE | <startedAt> | <notOverdueExpectedAt> | 200.00        |           | <startedAt> |
    When a GET request has been made to "/api/rentals" endpoint with query parameters
      | status |
      | ACTIVE |
    Then the response status is 200
    And the rental summary response only contains page of
      | id | customerId | equipmentIds | status | startedAt   | expectedReturnAt       | overdueMin       |
      | 1  | CUS1       | 1            | ACTIVE | <startedAt> | <overdueExpectedAt>    | <overdueMinutes> |
      | 2  | CUS2       | 2            | ACTIVE | <startedAt> | <notOverdueExpectedAt> | 0                |
    Examples:
      | now                 | startedAt           | overdueExpectedAt   | notOverdueExpectedAt | overdueMinutes |
      | 2026-02-18T12:00:00 | 2026-02-18T09:00:00 | 2026-02-18T10:00:00 | 2026-02-18T13:00:00  | 120            |

  Scenario: Get active rentals with pagination
    Given now is "2026-02-18T10:00:00"
    And rentals exist in the database with the following data
      | id | customerId | equipmentId | equipmentUid | tariffId | status | startedAt           | expectedReturnAt    | createdAt           | updatedAt           |
      | 1  | CUS1       | 1           | BIKE-001     | 1        | ACTIVE | 2026-02-18T09:00:00 | 2026-02-18T11:00:00 | 2026-02-18T10:00:00 | 2026-02-18T10:00:00 |
      | 2  | CUS2       | 2           | E-BIKE-001   | 1        | ACTIVE | 2026-02-18T09:00:00 | 2026-02-18T11:00:00 | 2026-02-18T10:00:00 | 2026-02-18T10:00:00 |
    When a GET request has been made to "/api/rentals" endpoint with query parameters
      | status | page | size |
      | ACTIVE | 0    | 1    |
    Then the response status is 200
    And the response contains
      | path               | value |
      | $.totalItems       | 2     |
      | $.pageRequest.size | 1     |
      | $.pageRequest.page | 0     |

  Scenario: Get rentals with non-existent status returns empty page
    Given now is "2026-02-18T10:00:00"
    And rental exists in the database with the following data
      | id | customerId | equipmentId | equipmentUid | tariffId | status | startedAt           | expectedReturnAt    | createdAt           | updatedAt           |
      | 1  | CUS1       | 1           | BIKE-001     | 1        | ACTIVE | 2026-02-18T09:00:00 | 2026-02-18T11:00:00 | 2026-02-18T10:00:00 | 2026-02-18T10:00:00 |
    When a GET request has been made to "/api/rentals" endpoint with query parameters
      | status    |
      | CANCELLED |
    Then the response status is 200
    And the response contains
      | path         | value |
      | $.totalItems | 0     |

  Scenario Outline: Get rentals filtered only by customerId
    Given now is "<now>"
    And rentals exist in the database with the following data
      | id | customerId | status    | startedAt   | expectedReturnAt   | createdAt   | updatedAt   |
      | 1  | CUS1       | ACTIVE    | <startedAt> | <expectedReturnAt> | <startedAt> | <startedAt> |
      | 2  | CUS2       | ACTIVE    | <startedAt> | <expectedReturnAt> | <startedAt> | <startedAt> |
      | 3  | CUS1       | COMPLETED | <startedAt> | <expectedReturnAt> | <startedAt> | <startedAt> |
      | 4  | CUS1       | DRAFT     | null        | null               | <startedAt> | <startedAt> |
    And rental equipment exists in the database with the following data
      | rentalId | equipmentId | equipmentUid | equipmentType | tariffId | status   | startedAt   | expectedReturnAt   | estimatedCost | finalCost | createdAt   |
      | 1        | 1           | BIKE-001     | BICYCLE       | 1        | ACTIVE   | <startedAt> | <expectedReturnAt> | 200.00        |           | <startedAt> |
      | 2        | 2           | E-BIKE-001   | SCOOTER       | 2        | ACTIVE   | <startedAt> | <expectedReturnAt> | 200.00        |           | <startedAt> |
      | 3        | 3           | E-BIKE-002   | SCOOTER       | 2        | RETURNED | <startedAt> | <expectedReturnAt> | 200.00        | 200.00    | <startedAt> |
      | 4        | 1           | BIKE-001     | BICYCLE       | 1        | ASSIGNED |             |                    |               |           | <startedAt> |
    When a GET request has been made to "/api/rentals" endpoint with query parameters
      | customerId |
      | CUS1       |
    Then the response status is 200
    And the rental summary response only contains page of
      | id | customerId | equipmentIds | status    | startedAt   | expectedReturnAt   | overdueMin |
      | 1  | CUS1       | 1            | ACTIVE    | <startedAt> | <expectedReturnAt> | 60         |
      | 3  | CUS1       | 3            | COMPLETED | <startedAt> | <expectedReturnAt> | 0          |
      | 4  | CUS1       | 1            | DRAFT     | null        | null               | 0          |
    And the response contains
      | path                 | value            |
      | $.totalItems         | 3                |
      | $.pageRequest.size   | 20               |
      | $.pageRequest.page   | 0                |
      | $.pageRequest.sortBy | expectedReturnAt |
    Examples:
      | now                 | startedAt           | expectedReturnAt    |
      | 2026-02-18T12:00:00 | 2026-02-18T09:00:00 | 2026-02-18T11:00:00 |

  Scenario Outline: Get active rentals filtered by equipmentUid
    Given now is "<now>"
    And rentals exist in the database with the following data
      | id | customerId | equipmentId | equipmentUid | tariffId | status | startedAt   | expectedReturnAt   | createdAt   | updatedAt   |
      | 1  | CUS1       | 1           | BIKE-001     | 1        | ACTIVE | <startedAt> | <expectedReturnAt> | <startedAt> | <startedAt> |
      | 2  | CUS2       | 2           | E-BIKE-001   | 1        | ACTIVE | <startedAt> | <expectedReturnAt> | <startedAt> | <startedAt> |
      | 3  | CUS1       | 1           | BIKE-001     | 1        | DRAFT  | null        | null               | <startedAt> | <startedAt> |
    And rental equipment exists in the database with the following data
      | rentalId | equipmentId | equipmentUid | equipmentType | tariffId | status   | startedAt   | expectedReturnAt   | estimatedCost | createdAt   |
      | 1        | 1           | BIKE-001     | BICYCLE       | 1        | ACTIVE   | <startedAt> | <expectedReturnAt> | 200.00        | <startedAt> |
      | 2        | 2           | E-BIKE-001   | SCOOTER       | 2        | ACTIVE   | <startedAt> | <expectedReturnAt> | 200.00        | <startedAt> |
      | 3        | 1           | BIKE-001     | BICYCLE       | 1        | ASSIGNED |             |                    |               | <startedAt> |
    When a GET request has been made to "/api/rentals" endpoint with query parameters
      | status | equipmentUid |
      | ACTIVE | E-BIKE-001   |
    Then the response status is 200
    And the rental summary response only contains page of
      | id | customerId | equipmentIds | status | startedAt   | expectedReturnAt   | overdueMin |
      | 2  | CUS2       | 2            | ACTIVE | <startedAt> | <expectedReturnAt> | 60         |
    And the response contains
      | path                 | value            |
      | $.totalItems         | 1                |
      | $.pageRequest.size   | 20               |
      | $.pageRequest.page   | 0                |
      | $.pageRequest.sortBy | expectedReturnAt |
    Examples:
      | now                 | startedAt           | expectedReturnAt    |
      | 2026-02-18T12:00:00 | 2026-02-18T09:00:00 | 2026-02-18T11:00:00 |

  Scenario: Get rentals filtered by equipmentUid returns empty when no match
    Given now is "2026-02-18T10:00:00"
    And rentals exist in the database with the following data
      | id | customerId | equipmentId | equipmentUid | tariffId | status | startedAt           | expectedReturnAt    | createdAt           | updatedAt           |
      | 1  | CUS1       | 1           | BIKE-001     | 1        | ACTIVE | 2026-02-18T09:00:00 | 2026-02-18T11:00:00 | 2026-02-18T10:00:00 | 2026-02-18T10:00:00 |
    When a GET request has been made to "/api/rentals" endpoint with query parameters
      | status | equipmentUid |
      | ACTIVE | NONEXISTENT  |
    Then the response status is 200
    And the response contains
      | path         | value |
      | $.totalItems | 0     |
