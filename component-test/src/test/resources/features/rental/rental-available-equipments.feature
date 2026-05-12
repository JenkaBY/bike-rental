Feature: Available equipment query
  As a rental staff member
  I want to query available equipment for a new rental
  So that I can present only genuinely available options before creating a rental

  Background:
    Given the request header "Content-Type" is "application/vnd.bikerental.v1+json"
    And customers exist in the database with the following data
      | id   | phone        | firstName | lastName | email | birthDate | comments |
      | CUS1 | +79995551111 | Alex      | Johnson  | null  | null      | null     |
    And the following equipment statues exist in the database
      | slug      | name      | description       | transitions      |
      | BROKEN    | Broken    | Not Ready to rent | AVAILABLE        |
      | AVAILABLE | Available | Ready to rent     | BROKEN,RENTED    |
      | RENTED    | Rented    | In use already    | AVAILABLE,BROKEN |
    And the following equipment types exist in the database
      | slug    | name    | description |
      | BICYCLE | Bicycle | Two-wheeled |
      | SCOOTER | Scooter | Electric    |
    And the following equipment records exist in db
      | id | serialNumber | uid        | status    | type    | model      | condition |
      | 1  | EQ-001       | BIKE-001   | AVAILABLE | BICYCLE | Model A    | GOOD      |
      | 2  | EQ-002       | E-BIKE-001 | RENTED    | SCOOTER | Model B    | GOOD      |
      | 3  | EQ-005       | BIKE-003   | AVAILABLE | BICYCLE | Model C    | GOOD      |
      | 4  | EQ-004       | BIKE-002   | AVAILABLE | BICYCLE | Model C    | BROKEN    |
      | 5  | EQ-0066      | BIKE-00-   | AVAILABLE | BICYCLE | Model 1    | GOOD      |
      | 6  | EQ-007       | BIKE-0066  | AVAILABLE | BICYCLE | Model 2    | GOOD      |
      | 7  | EQ-009       | BIKE-009   | RENTED    | BICYCLE | Model 0066 | GOOD      |
    And rentals exist in the database with the following data
      | id | customerId | status | startedAt           | expectedReturnAt    | createdAt           | updatedAt           |
      | 1  | CUS1       | ACTIVE | 2026-01-01T10:00:00 | 2026-01-01T12:00:00 | 2026-01-01T10:00:00 | 2026-01-01T10:00:00 |
      | 2  | CUS1       | DRAFT  | 2026-01-01T10:00:00 | 2026-01-01T12:00:00 | 2026-01-01T10:00:00 | 2026-01-01T10:00:00 |
    And rental equipment exists in the database with the following data
      | rentalId | equipmentId | equipmentUid | equipmentType | status   | startedAt           | createdAt           |
      | 1        | 2           | E-BIKE-001   | SCOOTER       | ACTIVE   | 2026-01-01T10:00:00 | 2026-01-01T10:00:00 |
      | 2        | 7           | BIKE-009     | BICYCLE       | ASSIGNED |                     | 2026-01-01T10:00:00 |

  Scenario: Returns all available equipment when no filter is applied
    When a GET request has been made to "/api/rentals/available-equipments" endpoint
    Then the response status is 200
    And the available equipment response only contains page of
      | id | uid       | serialNumber | type    | model   |
      | 1  | BIKE-001  | EQ-001       | BICYCLE | Model A |
      | 3  | BIKE-003  | EQ-005       | BICYCLE | Model C |
      | 5  | BIKE-00-  | EQ-0066      | BICYCLE | Model 1 |
      | 6  | BIKE-0066 | EQ-007       | BICYCLE | Model 2 |
    And the response contains
      | path               | value |
      | $.totalItems       | 4     |
      | $.pageRequest.page | 0     |
      | $.pageRequest.size | 20    |

  Scenario: Filters by query text and excludes occupied equipment from results
    When a GET request has been made to "/api/rentals/available-equipments" endpoint with query parameters
      | q    |
      | 0066 |
    Then the response status is 200
    And the available equipment response only contains page of
      | id | uid       | serialNumber | type    | model   |
      | 5  | BIKE-00-  | EQ-0066      | BICYCLE | Model 1 |
      | 6  | BIKE-0066 | EQ-007       | BICYCLE | Model 2 |
    And the response contains
      | path         | value |
      | $.totalItems | 2     |

  Scenario: Returns empty result when all matched equipment is occupied
    When a GET request has been made to "/api/rentals/available-equipments" endpoint with query parameters
      | q      |
      | E-BIKE |
    Then the response status is 200
    And the response contains
      | path         | value |
      | $.totalItems | 0     |

  Scenario: BROKEN condition equipment is always excluded regardless of occupancy
    When a GET request has been made to "/api/rentals/available-equipments" endpoint with query parameters
      | q      |
      | EQ-004 |
    Then the response status is 200
    And the response contains
      | path         | value |
      | $.totalItems | 0     |

  Scenario: Pagination parameters are reflected in the response and limit the returned items
    When a GET request has been made to "/api/rentals/available-equipments" endpoint with query parameters
      | page | size |
      | 0    | 2    |
    Then the response status is 200
    And the response contains
      | path               | value |
      | $.totalItems       | 4     |
      | $.pageRequest.page | 0     |
      | $.pageRequest.size | 2     |
    And the response list at "$.items" has size 2