@ReinitializeSystemLedgers
Feature: Rental Agreement Signing
  As a customer assisted by an operator
  I want to sign the active rental agreement
  So that the rental becomes ACTIVE with an immutable signed PDF record

  Background:
    Given the request header "Content-Type" is "application/vnd.bikerental.v1+json"
    And customers exist in the database with the following data
      | id   | phone        | firstName | lastName | email | birthDate | comments |
      | CUS1 | +79995551111 | Alex      | Johnson  | null  | null      | null     |
    And the following equipment statues exist in the database
      | slug      | name      | description   | transitions      |
      | AVAILABLE | Available | Ready to rent | RENTED,RESERVED  |
      | RESERVED  | Reserved  | Ready to rent | AVAILABLE,RENTED |
      | RENTED    | Rented    | In use        | AVAILABLE        |
    And the following equipment types exist in the database
      | slug    | name    | description |
      | BICYCLE | Bicycle | Two-wheeled |
      | HELMET  | Helmet  | Helmet      |
      | OTHER   | Other   | Other       |
    And the following equipment records exist in db
      | id | serialNumber | uid      | status    | type    | model   | conditionNotes | condition |
      | 1  | EQ-001       | BIKE-001 | AVAILABLE | BICYCLE | Model A | Good           | GOOD      |
    And the following account records exist in db
      | id   | accountType | customerId |
      | ACC1 | CUSTOMER    | CUS1       |
    And the following sub-ledger records exist in db
      | id     | accountId | ledgerType      | balance | version | createdAt            | updatedAt            |
      | L_C_W1 | ACC1      | CUSTOMER_WALLET | 284.00  | 2       | 2026-03-27T00:00:00Z | 2026-04-07T10:31:02Z |
      | L_C_H1 | ACC1      | CUSTOMER_HOLD   | 16.00   | 2       | 2026-03-27T00:00:00Z | 2026-04-07T10:30:00Z |
    And the pricing params list for tariff request is
      | tariffId | pricingType       | firstHourPrice | hourlyDiscount | minimumHourlyPrice | minimumDurationMinutes | minimumDurationSurcharge |
      | 10       | DEGRESSIVE_HOURLY | 9.00           | 2.00           | 1.00               | 30                     | 1.00                     |
    And the following tariff v2 records exist in db
      | id | name                   | description               | equipmentType | pricingType       | status | validFrom  | validTo |
      | 10 | Degressive Hourly Bike | Bicycle degressive hourly | BICYCLE       | DEGRESSIVE_HOURLY | ACTIVE | 2026-01-01 |         |
    And agreement templates exist in the database with the following data
      | id | versionNumber | title               | content                                                                            | contentSha256 | status | createdAt           | updatedAt           | activatedAt         |
      | 5  | 3             | Rental Agreement v3 | Dear {{customer.firstName}} {{customer.lastName}}, you agree to return it on time. | SHA_ZERO      | ACTIVE | 2026-01-01T09:00:00 | 2026-01-01T09:00:00 | 2026-01-01T09:00:00 |

  Scenario: Happy path - signing activates the rental and stores the PDF
    Given a single rental exists in the database with the following data
      | id | customerId | status             | plannedDuration | version | createdAt           | updatedAt           |
      | 1  | CUS1       | AWAITING_SIGNATURE | 120             | 1       | 2026-04-28T09:00:00 | 2026-04-28T09:00:00 |
    And rental equipments exist in the database with the following data
      | rentalId | equipmentId | equipmentUid | equipmentType | tariffId | status   | startedAt           | expectedReturnAt    | estimatedCost | createdAt           | updatedAt           |
      | 1        | 1           | BIKE-001     | BICYCLE       | 10       | ASSIGNED | 2026-04-28T09:00:00 | 2026-04-28T11:00:00 | 16.00         | 2026-04-28T09:00:00 | 2026-04-28T09:00:00 |
    And the sign agreement request is
      | rentalVersion | templateId | operatorId |
      | 1             | 5          | OP1       |
    When a POST request has been made to "/api/rentals/1/signatures" endpoint
    Then the response status is 201
    And the signature response contains a signature id and signed timestamp
    And rental was persisted in database
      | customerId | status |
      | CUS1       | ACTIVE |
    When a GET request for "application/pdf" content has been made to "/api/rentals/1/signatures" endpoint
    Then the response status is 200
    And the response headers contain
      | name                | value                                          |
      | Content-Disposition | attachment; filename="rental-1-agreement.pdf" |
    And the PDF body is a valid document containing text "Johnson"
    And the PDF body is a valid document containing text "1. BICYCLE(BIKE-001) — 16.00 BYN"
    And the PDF body is a valid document containing text "02:00 h"
    And the PDF body is a valid document containing text "Rental Agreement v3 dated 01.01.2026"

  Scenario: Version mismatch is rejected
    Given a single rental exists in the database with the following data
      | id | customerId | status             | plannedDuration | version | createdAt           | updatedAt           |
      | 1  | CUS1       | AWAITING_SIGNATURE | 120             | 2       | 2026-04-28T09:00:00 | 2026-04-28T09:00:00 |
    And rental equipments exist in the database with the following data
      | rentalId | equipmentId | equipmentUid | equipmentType | tariffId | status   | startedAt           | expectedReturnAt    | estimatedCost | createdAt           | updatedAt           |
      | 1        | 1           | BIKE-001     | BICYCLE       | 10       | ASSIGNED | 2026-04-28T09:00:00 | 2026-04-28T11:00:00 | 16.00         | 2026-04-28T09:00:00 | 2026-04-28T09:00:00 |
    And the sign agreement request is
      | rentalVersion | templateId | operatorId |
      | 1             | 5          | OP1       |
    When a POST request has been made to "/api/rentals/1/signatures" endpoint
    Then the response status is 409
    And the response contains
      | path        | value                                     |
      | $.errorCode | agreement.signing.rental_version_mismatch |

  Scenario: Duplicate signing is rejected
    Given a single rental exists in the database with the following data
      | id | customerId | status             | plannedDuration | version | createdAt           | updatedAt           |
      | 1  | CUS1       | AWAITING_SIGNATURE | 120             | 1       | 2026-04-28T09:00:00 | 2026-04-28T09:00:00 |
    And rental equipments exist in the database with the following data
      | rentalId | equipmentId | equipmentUid | equipmentType | tariffId | status   | startedAt           | expectedReturnAt    | estimatedCost | createdAt           | updatedAt           |
      | 1        | 1           | BIKE-001     | BICYCLE       | 10       | ASSIGNED | 2026-04-28T09:00:00 | 2026-04-28T11:00:00 | 16.00         | 2026-04-28T09:00:00 | 2026-04-28T09:00:00 |
    And the sign agreement request is
      | rentalVersion | templateId | operatorId |
      | 1             | 5          | OP1       |
    When a POST request has been made to "/api/rentals/1/signatures" endpoint
    Then the response status is 201
    Given the sign agreement request is
      | rentalVersion | templateId | operatorId |
      | 1             | 5          | OP1       |
    When a POST request has been made to "/api/rentals/1/signatures" endpoint
    Then the response status is 409
    And the response contains
      | path        | value                            |
      | $.errorCode | agreement.signing.already_signed |

  Scenario: Signing a draft rental is rejected
    Given a single rental exists in the database with the following data
      | id | customerId | status | plannedDuration | version | createdAt           | updatedAt           |
      | 1  | CUS1       | DRAFT  | 120             | 0       | 2026-04-28T09:00:00 | 2026-04-28T09:00:00 |
    And rental equipments exist in the database with the following data
      | rentalId | equipmentId | equipmentUid | equipmentType | tariffId | status   | startedAt           | expectedReturnAt    | estimatedCost | createdAt           | updatedAt           |
      | 1        | 1           | BIKE-001     | BICYCLE       | 10       | ASSIGNED | 2026-04-28T09:00:00 | 2026-04-28T11:00:00 | 16.00         | 2026-04-28T09:00:00 | 2026-04-28T09:00:00 |
    And the sign agreement request is
      | rentalVersion | templateId | operatorId |
      | 0             | 5          | OP1       |
    When a POST request has been made to "/api/rentals/1/signatures" endpoint
    Then the response status is 409
    And the response contains
      | path        | value                                           |
      | $.errorCode | agreement.signing.rental_not_awaiting_signature |

  Scenario: Signing with a non-active template is rejected
    Given a single rental exists in the database with the following data
      | id | customerId | status             | plannedDuration | version | createdAt           | updatedAt           |
      | 1  | CUS1       | AWAITING_SIGNATURE | 120             | 1       | 2026-04-28T09:00:00 | 2026-04-28T09:00:00 |
    And rental equipments exist in the database with the following data
      | rentalId | equipmentId | equipmentUid | equipmentType | tariffId | status   | startedAt           | expectedReturnAt    | estimatedCost | createdAt           | updatedAt           |
      | 1        | 1           | BIKE-001     | BICYCLE       | 10       | ASSIGNED | 2026-04-28T09:00:00 | 2026-04-28T11:00:00 | 16.00         | 2026-04-28T09:00:00 | 2026-04-28T09:00:00 |
    And the sign agreement request is
      | rentalVersion | templateId | operatorId |
      | 1             | 99         | OP1       |
    When a POST request has been made to "/api/rentals/1/signatures" endpoint
    Then the response status is 409
    And the response contains
      | path        | value                         |
      | $.errorCode | agreement.template.not_active |

  Scenario: Listing and download reflect signing state
    Given a single rental exists in the database with the following data
      | id | customerId | status             | plannedDuration | version | createdAt           | updatedAt           |
      | 1  | CUS1       | AWAITING_SIGNATURE | 120             | 1       | 2026-04-28T09:00:00 | 2026-04-28T09:00:00 |
    And rental equipments exist in the database with the following data
      | rentalId | equipmentId | equipmentUid | equipmentType | tariffId | status   | startedAt           | expectedReturnAt    | estimatedCost | createdAt           | updatedAt           |
      | 1        | 1           | BIKE-001     | BICYCLE       | 10       | ASSIGNED | 2026-04-28T09:00:00 | 2026-04-28T11:00:00 | 16.00         | 2026-04-28T09:00:00 | 2026-04-28T09:00:00 |
    When a GET request has been made to "/api/rentals/1/signatures" endpoint
    Then the response status is 200
    And the signature list has size 0
    When a GET request for "application/pdf" content has been made to "/api/rentals/1/signatures" endpoint
    Then the response status is 404
    Given the sign agreement request is
      | rentalVersion | templateId | operatorId |
      | 1             | 5          | OP1       |
    When a POST request has been made to "/api/rentals/1/signatures" endpoint
    Then the response status is 201
    When a GET request has been made to "/api/rentals/1/signatures" endpoint
    Then the response status is 200
    And the signature list contains
      | templateId | templateVersionNumber |
      | 5          | 3                     |
    When a GET request for "application/pdf" content has been made to "/api/rentals/1/signatures" endpoint
    Then the response status is 200

  Scenario: Rendered agreement text has placeholders substituted for the awaiting-signature rental
    Given a single rental exists in the database with the following data
      | id | customerId | status             | plannedDuration | version | createdAt           | updatedAt           |
      | 1  | CUS1       | AWAITING_SIGNATURE | 120             | 1       | 2026-04-28T09:00:00 | 2026-04-28T09:00:00 |
    And rental equipments exist in the database with the following data
      | rentalId | equipmentId | equipmentUid | equipmentType | tariffId | status   | startedAt           | expectedReturnAt    | estimatedCost | createdAt           | updatedAt           |
      | 1        | 1           | BIKE-001     | BICYCLE       | 10       | ASSIGNED | 2026-04-28T09:00:00 | 2026-04-28T11:00:00 | 16.00         | 2026-04-28T09:00:00 | 2026-04-28T09:00:00 |
    When a GET request has been made to "/api/rentals/1/agreement" endpoint
    Then the response status is 200
    And the rental agreement response contains
      | templateId | versionNumber | title               |
      | 5          | 3             | Rental Agreement v3 |
    And the rental agreement content contains "Dear Alex Johnson, you agree to return it on time."
    And the rental agreement content does not contain "{{"

  Scenario: Rendered agreement text is rejected for a draft rental
    Given a single rental exists in the database with the following data
      | id | customerId | status | plannedDuration | version | createdAt           | updatedAt           |
      | 1  | CUS1       | DRAFT  | 120             | 0       | 2026-04-28T09:00:00 | 2026-04-28T09:00:00 |
    And rental equipments exist in the database with the following data
      | rentalId | equipmentId | equipmentUid | equipmentType | tariffId | status   | startedAt           | expectedReturnAt    | estimatedCost | createdAt           | updatedAt           |
      | 1        | 1           | BIKE-001     | BICYCLE       | 10       | ASSIGNED | 2026-04-28T09:00:00 | 2026-04-28T11:00:00 | 16.00         | 2026-04-28T09:00:00 | 2026-04-28T09:00:00 |
    When a GET request has been made to "/api/rentals/1/agreement" endpoint
    Then the response status is 409
    And the response contains
      | path        | value                                           |
      | $.errorCode | agreement.signing.rental_not_awaiting_signature |
