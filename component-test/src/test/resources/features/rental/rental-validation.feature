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
      | BICYCLE | Bicycle | Two-wheeled |
      | SCOOTER | Scooter | Scooter     |
    And the following equipment records exist in db
      | id | serialNumber | uid        | status    | type    | model   | conditionNotes | condition |
      | 1  | EQ-001       | BIKE-001   | AVAILABLE | BICYCLE | Model A | Good           | GOOD      |
      | 2  | EQ-002       | E-BIKE-001 | RENTED    | SCOOTER | Model B | Excellent      | GOOD      |
      | 3  | EQ-003       | BIKE-002   | BROKEN    | BICYCLE | Model C | Poor           | BROKEN    |


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
      | id | customerId | tariffId | status | createdAt           | updatedAt           |
      | 1  | CUS1       | 1        | DRAFT  | 2026-02-06T10:00:00 | 2026-02-06T10:00:00 |
    And rental equipment exists in the database with the following data
      | rentalId | equipmentId | equipmentUid | equipmentType | tariffId | status   | startedAt           | expectedReturnAt    | estimatedCost | createdAt           | updatedAt           |
      | 1        | 1           | BIKE-001     | BICYCLE       | 1        | ASSIGNED | 2026-02-10T08:00:00 | 2026-02-10T10:00:00 | 200.00        | 2026-02-10T08:00:00 | 2026-02-10T08:00:00 |
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
      | id | customerId | tariffId | status | createdAt           | updatedAt           |
      | 1  | CUS1       | 1        | DRAFT  | 2026-02-06T10:00:00 | 2026-02-06T10:00:00 |
    And rental equipment exists in the database with the following data
      | rentalId | equipmentId | equipmentUid | equipmentType | tariffId | status   | startedAt           | expectedReturnAt    | estimatedCost | createdAt           | updatedAt           |
      | 1        | 1           | BIKE-001     | BICYCLE       | 1        | ASSIGNED | 2026-02-10T08:00:00 | 2026-02-10T10:00:00 | 200.00        | 2026-02-10T08:00:00 | 2026-02-10T08:00:00 |
    And the rental update request is
      | op      | path          | value |
      | replace | /equipmentIds | [999] |
    When a PATCH request has been made to "/api/rentals/{requestedObjectId}" endpoint with context
    Then the response status is 422
    And the response contains
      | path     | value                                                  |
      | $.title  | Unprocessable Content                                  |
      | $.detail | Referenced Equipment with identifier '[999]' not found |

  Scenario: Update rental with rented equipment
    Given a single rental exists in the database with the following data
      | id | customerId | tariffId | status | createdAt           | updatedAt           |
      | 1  | CUS2       | 1        | DRAFT  | 2026-02-06T10:00:00 | 2026-02-06T10:00:00 |
    And a single rental exists in the database with the following data
      | id | customerId | tariffId | status | createdAt           | updatedAt           |
      | 2  | CUS1       | 1        | DRAFT  | 2026-02-06T10:00:00 | 2026-02-06T10:00:00 |
    And rental equipments exist in the database with the following data
      | rentalId | equipmentId | equipmentUid | equipmentType | tariffId | status   | startedAt           | expectedReturnAt    | estimatedCost | createdAt           | updatedAt           |
      | 1        | 2           | BIKE-001     | BICYCLE       | 1        | ASSIGNED | 2026-02-10T08:00:00 | 2026-02-10T10:00:00 | 200.00        | 2026-02-10T08:00:00 | 2026-02-10T08:00:00 |
    And the rental update request is
      | op      | path          | value |
      | replace | /equipmentIds | [2]   |
    When a PATCH request has been made to "/api/rentals/{requestedObjectId}" endpoint with context
    Then the response status is 409
    And the response contains
      | path        | value                                                                   |
      | $.title     | Equipment not available                                                 |
      | $.detail    | Requested equipment is already occupied in an active or assigned rental |
      | $.errorCode | rental.equipment.not_available                                          |

  Scenario: Update rental with broken equipment
    Given a single rental exists in the database with the following data
      | id | customerId | tariffId | status | createdAt           | updatedAt           |
      | 1  | CUS1       | 1        | DRAFT  | 2026-02-06T10:00:00 | 2026-02-06T10:00:00 |
    And rental equipment exists in the database with the following data
      | rentalId | equipmentId | equipmentUid | equipmentType | tariffId | status   | startedAt           | expectedReturnAt    | estimatedCost | createdAt           | updatedAt           |
      | 1        | 1           | BIKE-001     | BICYCLE       | 1        | ASSIGNED | 2026-02-10T08:00:00 | 2026-02-10T10:00:00 | 200.00        | 2026-02-10T08:00:00 | 2026-02-10T08:00:00 |
    And the rental update request is
      | op      | path          | value |
      | replace | /equipmentIds | [3]   |
    When a PATCH request has been made to "/api/rentals/{requestedObjectId}" endpoint with context
    Then the response status is 422
    And the response contains
      | path     | value                                        |
      | $.title  | Unprocessable Content                        |
      | $.detail | Equipments with ids [3] is not in GOOD state |

  Scenario Outline: Update rental with incorrect duration
    Given a single rental exists in the database with the following data
      | id | customerId | tariffId | status | createdAt           | updatedAt           |
      | 1  | CUS1       | 1        | DRAFT  | 2026-02-06T10:00:00 | 2026-02-06T10:00:00 |
    And rental equipment exists in the database with the following data
      | rentalId | equipmentId | equipmentUid | equipmentType | tariffId | status   | startedAt           | expectedReturnAt    | estimatedCost | createdAt           | updatedAt           |
      | 1        | 1           | BIKE-001     | BICYCLE       | 1        | ASSIGNED | 2026-02-10T08:00:00 | 2026-02-10T10:00:00 | 200.00        | 2026-02-10T08:00:00 | 2026-02-10T08:00:00 |
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

  @ResetClock
  Scenario: Update rental equipment when no suitable tariff found for new equipment type
    Given today is "2026-02-09"
    And the following equipment records exist in db
      | id | serialNumber | uid      | status    | type    | model   | conditionNotes | condition |
      | 4  | EQ-004       | BIKE-004 | AVAILABLE | SCOOTER | Model D | Good           | GOOD      |
    And a single rental exists in the database with the following data
      | id | customerId | tariffId | status | plannedDuration | createdAt           | updatedAt           |
      | 1  | CUS1       | 1        | DRAFT  | 120             | 2026-02-06T10:00:00 | 2026-02-06T10:00:00 |
    And rental equipment exists in the database with the following data
      | rentalId | equipmentId | equipmentUid | equipmentType | tariffId | status   | startedAt           | expectedReturnAt    | estimatedCost | createdAt           | updatedAt           |
      | 1        | 1           | BIKE-001     | BICYCLE       | 1        | ASSIGNED | 2026-02-10T08:00:00 | 2026-02-10T10:00:00 | 200.00        | 2026-02-10T08:00:00 | 2026-02-10T08:00:00 |
    And the rental update request is
      | op      | path          | value |
      | replace | /equipmentIds | [4]   |
    When a PATCH request has been made to "/api/rentals/{requestedObjectId}" endpoint with context
    Then the response status is 404
    And the response contains
      | path     | value                                                                                              |
      | $.title  | Suitable tariff not found                                                                          |
      | $.detail | No suitable tariff found for equipment type 'SCOOTER' on date 2026-02-09 for duration: 120 minutes |

  @ResetClock
#    tariff selection is performed when equipmentIds is present
  Scenario: Update rental duration when no suitable tariff found for equipment type
    Given today is "2026-02-09"
    And the following equipment records exist in db
      | id | serialNumber | uid      | status    | type    | model   | conditionNotes | condition |
      | 4  | EQ-004       | BIKE-004 | AVAILABLE | SCOOTER | Model D | Good           | GOOD      |
    And a single rental exists in the database with the following data
      | id | customerId | status | createdAt           | updatedAt           |
      | 1  | CUS1       | DRAFT  | 2026-02-06T10:00:00 | 2026-02-06T10:00:00 |
    And rental equipment exists in the database with the following data
      | rentalId | equipmentId | equipmentUid | equipmentType | tariffId | status   | startedAt           | expectedReturnAt    | estimatedCost | createdAt           | updatedAt           |
      | 1        | 3           | BIKE-004     | BICYCLE       | 3        | ASSIGNED | 2026-02-10T08:00:00 | 2026-02-10T10:00:00 | 0.00          | 2026-02-10T08:00:00 | 2026-02-10T08:00:00 |
    And the rental update request is
      | op      | path          | value |
      | replace | /duration     | 120   |
      | replace | /equipmentIds | [4]   |
    When a PATCH request has been made to "/api/rentals/{requestedObjectId}" endpoint with context
    Then the response status is 404
    And the response contains
      | path     | value                                                                                              |
      | $.title  | Suitable tariff not found                                                                          |
      | $.detail | No suitable tariff found for equipment type 'SCOOTER' on date 2026-02-09 for duration: 120 minutes |
