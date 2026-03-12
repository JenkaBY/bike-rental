Feature: Rental Update Validation
  As an operator
  I want to receive clear error messages when rental update fails validation
  So that I can correct invalid data

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
      | 2  | EQ-002       | E-BIKE-001 | RENTED    | scooter | Model B | Excellent |
      | 3  | EQ-003       | BIKE-002   | BROKEN    | bicycle | Model C | Poor      |
    And the following tariff records exist in db
      | id | name        | description     | equipmentType | basePrice | halfHourPrice | hourPrice | dayPrice | discountedPrice | validFrom  | validTo    | status |
      | 1  | Hourly Rate | Standard hourly | bicycle       | 100.00    | 60.00         | 100.00    | 500.00   | 90.00           | 2026-01-01 | 2026-12-31 | ACTIVE |
      | 2  | Daily Rate  | Standard daily  | bicycle       | 200.00    | 70.00         | 110.00    | 600.00   | 95.00           | 2026-01-01 | 2026-12-31 | ACTIVE |

  Scenario: Update non-existent rental
    Given the rental update request is
      | op      | path        | value |
      | replace | /customerId | CUS1  |
    When a PATCH request has been made to "/api/rentals/{id}" endpoint with
      | {id} |
      | 999  |
    Then the response status is 404
    And the response contains
      | path     | value                                  |
      | $.title  | Not Found                              |
      | $.detail | Rental with identifier '999' not found |

  Scenario: Update rental with non-existent customer
    Given a single rental exists in the database with the following data
      | customerId | equipmentId | tariffId | status | createdAt           | updatedAt           |
      | CUS1       | 1           | 1        | DRAFT  | 2026-02-06T10:00:00 | 2026-02-06T10:00:00 |
    And the rental update request is
      | op      | path        | value |
      | replace | /customerId | CUS3  |
    When a PATCH request has been made to "/api/rentals/{requestedObjectId}" endpoint with context
    Then the response status is 422
    And the response contains
      | path     | value                                                                                |
      | $.title  | Unprocessable Content                                                                |
      | $.detail | Referenced Customer with identifier '11111111-1111-1111-1111-111111111113' not found |

  Scenario: Update rental with non-existent equipment
    Given a single rental exists in the database with the following data
      | customerId | equipmentId | tariffId | status | createdAt           | updatedAt           |
      | CUS1       | 1           | 1        | DRAFT  | 2026-02-06T10:00:00 | 2026-02-06T10:00:00 |
    And the rental update request is
      | op      | path         | value |
      | replace | /equipmentId | 999   |
    When a PATCH request has been made to "/api/rentals/{requestedObjectId}" endpoint with context
    Then the response status is 422
    And the response contains
      | path     | value                                                |
      | $.title  | Unprocessable Content                                |
      | $.detail | Referenced Equipment with identifier '999' not found |

  # EquipmentNotAvailableException - Equipment not available (422)

  Scenario: Update rental with rented equipment
    Given a single rental exists in the database with the following data
      | customerId | equipmentId | tariffId | status | createdAt           | updatedAt           |
      | CUS1       | 1           | 1        | DRAFT  | 2026-02-06T10:00:00 | 2026-02-06T10:00:00 |
    And the rental update request is
      | op      | path         | value |
      | replace | /equipmentId | 2     |
    When a PATCH request has been made to "/api/rentals/{requestedObjectId}" endpoint with context
    Then the response status is 422
    And the response contains
      | path     | value                                                        |
      | $.title  | Unprocessable Content                                        |
      | $.detail | Equipment with id 2 is not available. Current status: RENTED |

  Scenario: Update rental with broken equipment
    Given a single rental exists in the database with the following data
      | customerId | equipmentId | tariffId | status | createdAt           | updatedAt           |
      | CUS1       | 1           | 1        | DRAFT  | 2026-02-06T10:00:00 | 2026-02-06T10:00:00 |
    And the rental update request is
      | op      | path         | value |
      | replace | /equipmentId | 3     |
    When a PATCH request has been made to "/api/rentals/{requestedObjectId}" endpoint with context
    Then the response status is 422
    And the response contains
      | path     | value                                                        |
      | $.title  | Unprocessable Content                                        |
      | $.detail | Equipment with id 3 is not available. Current status: BROKEN |

  Scenario Outline: Update rental with incorrect duration
    Given a single rental exists in the database with the following data
      | customerId | equipmentId | tariffId | status | createdAt           | updatedAt           |
      | CUS1       | 1           | 1        | DRAFT  | 2026-02-06T10:00:00 | 2026-02-06T10:00:00 |
    And the rental update request is
      | op      | path      | value      |
      | replace | /duration | <duration> |
    When a PATCH request has been made to "/api/rentals/{requestedObjectId}" endpoint with context
    Then the response status is 400
    And the response contains
      | path              | value                                     |
      | $.title           | Bad Request                               |
      | $.errorCode       | shared.method_arguments.validation_failed |
      | $.detail          | <detail>                                  |
      | $.errors[0].field | operations[0].value                       |
      | $.errors[0].code  | validation.valid_rental_patch_operation   |
    Examples:
      | duration | detail           |
      |          | Validation error |
      | any      | Validation error |

  Scenario: Update rental with non-existent tariff
    Given a single rental exists in the database with the following data
      | customerId | equipmentId | tariffId | status | createdAt           | updatedAt           |
      | CUS1       | 1           | 1        | DRAFT  | 2026-02-06T10:00:00 | 2026-02-06T10:00:00 |
    And the rental update request is
      | op      | path      | value |
      | replace | /tariffId | 999   |
    When a PATCH request has been made to "/api/rentals/{requestedObjectId}" endpoint with context
    Then the response status is 422
    And the response contains
      | path     | value                                             |
      | $.title  | Unprocessable Content                             |
      | $.detail | Referenced Tariff with identifier '999' not found |

  @ResetClock
  Scenario: Update rental equipment when no suitable tariff found for new equipment type
    Given today is "2026-02-09"
    And the following equipment records exist in db
      | id | serialNumber | uid      | status    | type    | model   | condition |
      | 4  | EQ-004       | BIKE-004 | AVAILABLE | scooter | Model D | Good      |
    And a single rental exists in the database with the following data
      | customerId | equipmentId | tariffId | status | plannedDuration | createdAt           | updatedAt           |
      | CUS1       | 1           | 1        | DRAFT  | 120             | 2026-02-06T10:00:00 | 2026-02-06T10:00:00 |
    And the rental update request is
      | op      | path         | value |
      | replace | /equipmentId | 4     |
    When a PATCH request has been made to "/api/rentals/{requestedObjectId}" endpoint with context
    Then the response status is 404
    And the response contains
      | path     | value                                                                                              |
      | $.title  | Suitable tariff not found                                                                          |
      | $.detail | No suitable tariff found for equipment type 'scooter' on date 2026-02-09 for duration: 120 minutes |

  @ResetClock
  Scenario: Update rental duration when no suitable tariff found for equipment type
    Given today is "2026-02-09"
    And the following equipment records exist in db
      | id | serialNumber | uid      | status    | type    | model   | condition |
      | 4  | EQ-004       | BIKE-004 | AVAILABLE | scooter | Model D | Good      |
    And a single rental exists in the database with the following data
      | customerId | equipmentId | tariffId | status | createdAt           | updatedAt           |
      | CUS1       | 4           |          | DRAFT  | 2026-02-06T10:00:00 | 2026-02-06T10:00:00 |
    And the rental update request is
      | op      | path      | value |
      | replace | /duration | PT2H  |
    When a PATCH request has been made to "/api/rentals/{requestedObjectId}" endpoint with context
    Then the response status is 404
    And the response contains
      | path     | value                                                                                              |
      | $.title  | Suitable tariff not found                                                                          |
      | $.detail | No suitable tariff found for equipment type 'scooter' on date 2026-02-09 for duration: 120 minutes |

  Scenario: Update rental duration when equipment is deleted
    Given a single rental exists in the database with the following data
      | customerId | equipmentId | tariffId | status | createdAt           | updatedAt           |
      | CUS1       | 999         |          | DRAFT  | 2026-02-06T10:00:00 | 2026-02-06T10:00:00 |
    And the rental update request is
      | op      | path      | value |
      | replace | /duration | PT2H  |
    When a PATCH request has been made to "/api/rentals/{requestedObjectId}" endpoint with context
    Then the response status is 422
    And the response contains
      | path     | value                                                |
      | $.title  | Unprocessable Content                                |
      | $.detail | Referenced Equipment with identifier '999' not found |
