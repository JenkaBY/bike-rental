@ReinitializeSystemLedgers @ResetClock
Feature: Add Equipment to an Active Rental
  As an operator
  I want to add new equipment to an already active rental
  So that a customer can pick up additional items during an ongoing rental

  Background:
    Given the request header "Content-Type" is "application/vnd.bikerental.v1+json"
    And customers exist in the database with the following data
      | id   | phone        | firstName | lastName | email            | birthDate  | comments |
      | CUS1 | +79995551111 | Alex      | Johnson  | null             | null       | null     |
      | CUS2 | +3706861555  | John      | Doe      | john@example.com | 1922-02-22 | null     |
    And the following equipment statues exist in the database
      | slug        | name        | description       | transitions               |
      | BROKEN      | Broken      | Not Ready to rent | AVAILABLE,MAINTENANCE     |
      | AVAILABLE   | Available   | Ready to rent     | BROKEN,MAINTENANCE,RENTED |
      | MAINTENANCE | Maintenance | null              | AVAILABLE                 |
      | RENTED      | Rented      | In use already    | AVAILABLE,BROKEN          |
    And the following equipment types exist in the database
      | slug    | name    | description |
      | BICYCLE | Bicycle | Two-wheeled |
      | HELMET  | Helmet  | Head gear   |
    And the following equipment records exist in db
      | id | serialNumber | uid        | status    | type    | model   | conditionNotes | condition |
      | 1  | EQ-001       | BIKE-001   | RENTED    | BICYCLE | Model A | Good           | GOOD      |
      | 2  | EQ-002       | HELMET-001 | AVAILABLE | HELMET  | Model H | Good           | GOOD      |
      | 3  | EQ-003       | BIKE-003   | BROKEN    | BICYCLE | Model A | Damaged        | BROKEN    |
    And the pricing params list for tariff request is
      | tariffId | pricingType       | firstHourPrice | hourlyDiscount | minimumHourlyPrice | minimumDurationMinutes | minimumDurationSurcharge | issuanceFee |
      | 1        | DEGRESSIVE_HOURLY | 9.00           | 2.00           | 1.00               | 30                     | 1.00                     |             |
      | 4        | FLAT_FEE          |                |                |                    |                        |                          | 1.00        |
    And the following tariff v2 records exist in db
      | id | name            | description       | equipmentType | pricingType       | status | validFrom  | validTo |
      | 1  | Hourly Bicycle  | Degressive hourly | BICYCLE       | DEGRESSIVE_HOURLY | ACTIVE | 2026-01-01 |         |
      | 4  | Flat Fee Helmet | Flat fee          | HELMET        | FLAT_FEE          | ACTIVE | 2026-01-01 |         |

  Scenario Outline: Add equipment to an active rental - estimate recalculated for the remaining window
    Given now is "<now>"
    And a single rental exists in the database with the following data
      | id         | customerId   | status | estimatedCost   | plannedDuration | startedAt   | expectedReturnAt    | createdAt   | updatedAt   |
      | <rentalId> | <customerId> | ACTIVE | <estimatedCost> | 120             | <startedAt> | <expectedReturnAt> | <startedAt> | <startedAt> |
    And rental equipments exist in the database with the following data
      | rentalId   | equipmentId   | equipmentUid   | equipmentType | tariffId | status | startedAt   | expectedReturnAt    | estimatedCost   | createdAt   | updatedAt   |
      | <rentalId> | <equipmentId> | <equipmentUid> | BICYCLE       | 1        | ACTIVE | <startedAt> | <expectedReturnAt> | <estimatedCost> | <startedAt> | <startedAt> |
    And the add equipment request is
      | equipmentIds       | operatorId   |
      | <addedEquipmentId> | <operatorId> |
    When a POST request has been made to "/api/rentals/{requestedObjectId}/equipments" endpoint with context
    Then the response status is 200
    And the response contains
      | path     | value  |
      | $.status | ACTIVE |
    And there're 2 rental equipments in database
    And rental equipments were persisted in database
      | rentalId   | equipmentId        | equipmentUid        | equipmentType | tariffId        | status | expectedReturnAt    | estimatedCost        |
      | <rentalId> | <equipmentId>      | <equipmentUid>      | BICYCLE       | 1               | ACTIVE | <expectedReturnAt> | <estimatedCost>      |
      | <rentalId> | <addedEquipmentId> | <addedEquipmentUid> | HELMET        | <addedTariffId> | ACTIVE | <expectedReturnAt> | <addedEstimatedCost> |
    Examples:
      | rentalId | customerId | now                 | startedAt           | expectedReturnAt    | estimatedCost | equipmentId | equipmentUid | addedEquipmentId | addedEquipmentUid | addedTariffId | addedEstimatedCost | operatorId |
      | 30       | CUS2       | 2026-02-10T09:00:00 | 2026-02-10T08:00:00 | 2026-02-10T10:00:00 | 16.00         | 1           | BIKE-001     | 2                 | HELMET-001         | 4             | 1.00               | OP1        |

  Scenario: Add already-rented equipment is rejected as occupied
    Given now is "2026-02-10T09:00:00"
    And a single rental exists in the database with the following data
      | id | customerId | status | estimatedCost | plannedDuration | startedAt           | expectedReturnAt    | createdAt           | updatedAt           |
      | 30 | CUS2       | ACTIVE | 16.00         | 120             | 2026-02-10T08:00:00 | 2026-02-10T10:00:00 | 2026-02-10T08:00:00 | 2026-02-10T08:00:00 |
    And rental equipments exist in the database with the following data
      | rentalId | equipmentId | equipmentUid | equipmentType | tariffId | status | startedAt           | expectedReturnAt    | estimatedCost | createdAt           | updatedAt           |
      | 30       | 1           | BIKE-001     | BICYCLE       | 1        | ACTIVE | 2026-02-10T08:00:00 | 2026-02-10T10:00:00 | 16.00         | 2026-02-10T08:00:00 | 2026-02-10T08:00:00 |
    And the add equipment request is
      | equipmentIds | operatorId |
      | 1            | OP1        |
    When a POST request has been made to "/api/rentals/{requestedObjectId}/equipments" endpoint with context
    Then the response status is 409
    And the response contains
      | path        | value                           |
      | $.title     | Equipment not available         |
      | $.errorCode | rental.equipment.not_available  |
    And there's 1 rental equipment in database

  Scenario: Add broken equipment is rejected
    Given now is "2026-02-10T09:00:00"
    And a single rental exists in the database with the following data
      | id | customerId | status | estimatedCost | plannedDuration | startedAt           | expectedReturnAt    | createdAt           | updatedAt           |
      | 30 | CUS2       | ACTIVE | 16.00         | 120             | 2026-02-10T08:00:00 | 2026-02-10T10:00:00 | 2026-02-10T08:00:00 | 2026-02-10T08:00:00 |
    And rental equipments exist in the database with the following data
      | rentalId | equipmentId | equipmentUid | equipmentType | tariffId | status | startedAt           | expectedReturnAt    | estimatedCost | createdAt           | updatedAt           |
      | 30       | 1           | BIKE-001     | BICYCLE       | 1        | ACTIVE | 2026-02-10T08:00:00 | 2026-02-10T10:00:00 | 16.00         | 2026-02-10T08:00:00 | 2026-02-10T08:00:00 |
    And the add equipment request is
      | equipmentIds | operatorId |
      | 3            | OP1        |
    When a POST request has been made to "/api/rentals/{requestedObjectId}/equipments" endpoint with context
    Then the response status is 422
    And there's 1 rental equipment in database

  Scenario: Add equipment to a rental whose window has already elapsed is rejected
    Given now is "2026-02-10T11:00:00"
    And a single rental exists in the database with the following data
      | id | customerId | status | estimatedCost | plannedDuration | startedAt           | expectedReturnAt    | createdAt           | updatedAt           |
      | 30 | CUS2       | ACTIVE | 16.00         | 120             | 2026-02-10T08:00:00 | 2026-02-10T10:00:00 | 2026-02-10T08:00:00 | 2026-02-10T08:00:00 |
    And rental equipments exist in the database with the following data
      | rentalId | equipmentId | equipmentUid | equipmentType | tariffId | status | startedAt           | expectedReturnAt    | estimatedCost | createdAt           | updatedAt           |
      | 30       | 1           | BIKE-001     | BICYCLE       | 1        | ACTIVE | 2026-02-10T08:00:00 | 2026-02-10T10:00:00 | 16.00         | 2026-02-10T08:00:00 | 2026-02-10T08:00:00 |
    And the add equipment request is
      | equipmentIds | operatorId |
      | 2            | OP1        |
    When a POST request has been made to "/api/rentals/{requestedObjectId}/equipments" endpoint with context
    Then the response status is 422
    And the response contains
      | path        | value                 |
      | $.title     | Rental window elapsed |
      | $.errorCode | rental.window.elapsed |
    And there's 1 rental equipment in database

  Scenario Outline: Add equipment to a rental that is not ACTIVE is rejected
    Given a single rental exists in the database with the following data
      | id | customerId | status   | plannedDuration | startedAt           | expectedReturnAt    | createdAt           | updatedAt           |
      | 31 | CUS2       | <status> | 120             | 2026-02-10T08:00:00 | 2026-02-10T10:00:00 | 2026-02-10T08:00:00 | 2026-02-10T08:00:00 |
    And the add equipment request is
      | equipmentIds | operatorId |
      | 2            | OP1        |
    When a POST request has been made to "/api/rentals/{requestedObjectId}/equipments" endpoint with context
    Then the response status is 422
    And the response contains
      | path        | value                                                                            |
      | $.title     | Invalid rental status                                                            |
      | $.detail    | Cannot perform operation on rental with status <status>. Expected status: ACTIVE |
      | $.errorCode | rental.status.invalid                                                            |
    Examples:
      | status    |
      | DRAFT     |
      | COMPLETED |
      | CANCELLED |
      | DEBT      |

  Scenario: Add equipment to a non-existing rental returns 404
    Given the add equipment request is
      | equipmentIds | operatorId |
      | 2            | OP1        |
    When a POST request has been made to "/api/rentals/999/equipments" endpoint
    Then the response status is 404
