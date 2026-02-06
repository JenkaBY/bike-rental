Feature: Rental Management
  As an operator
  I want to create and manage rentals
  So that I can track bike rental operations

  Background:
    Given the request header "Content-Type" is "application/vnd.bikerental.v1+json"
#    Commented out temporary
#    And customers exist in the database with the following data
#      | id   | phone        | firstName | lastName | email            | birthDate  | comments |
#      | CUS1 | +79995551111 | Alex      | Johnson  | null             | null       | null     |
#      | CUS2 | +79991232222 | John      | Doe      | john@example.com | 1922-02-22 | null     |
#    And the following equipment statues exist in the database
#      | slug        | name        | description       | transitions               |
#      | BROKEN      | Broken      | Not Ready to rent | AVAILABLE,MAINTENANCE     |
#      | AVAILABLE   | Available   | Ready to rent     | BROKEN,MAINTENANCE,RENTED |
#      | MAINTENANCE | Maintenance | null              | AVAILABLE                 |
#      | RENTED      | Rented      | In use already    | AVAILABLE,BROKEN          |
#    And the following equipment types exist in the database
#      | slug    | name    | description |
#      | bicycle | Bicycle | Two-wheeled |
#      | scooter | Scooter | Scooter     |
#    And the following equipment records exist in db
#      | id | serialNumber | uid        | status    | type    | model   | condition |
#      | 1  | EQ-001       | BIKE-001   | AVAILABLE | bicycle | Model A | Good      |
#      | 2  | EQ-002       | E-BIKE-001 | AVAILABLE | scooter | Model B | Excellent |
#    And the following tariff records exist in db
#      | name        | description     | equipmentType | basePrice | halfHourPrice | hourPrice | dayPrice | discountedPrice | validFrom  | validTo    | status   |
#      | Hourly Rate | Standard hourly | bicycle       | 100.00    | 60.00         | 100.00    | 500.00   | 90.00           | 2026-01-01 | 2026-12-31 | ACTIVE   |
#      | Daily Rate  | Standard daily  | bicycle       | 100.00    | 60.00         | 100.00    | 500.00   | 90.00           | 2026-01-01 | 2026-12-31 | ACTIVE   |

  Scenario: Create rental draft
    When a POST request has been made to "/api/rentals" endpoint
    Then the response status is 201
    And the rental response only contains
      | status |
      | DRAFT  |

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
