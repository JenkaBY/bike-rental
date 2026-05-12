# Task 004: Create rental-lifecycle.feature

> **Applied Skill:** `spring-boot-java-cucumber/SKILL.md` — Gherkin conventions, Background setup,
> DataTable assertions, reuse of existing steps

## 1. Objective

Create the Gherkin feature file covering the three happy-path scenarios for the new lifecycle
endpoint. All infrastructure setup in `Background:` is identical to `rental.feature`.

## 2. File to Modify / Create

* **File Path:** `component-test/src/test/resources/features/rental/rental-lifecycle.feature`
* **Action:** Create New File

**Code to Add/Replace:**

```gherkin
@ReinitializeSystemLedgers
Feature: Rental Lifecycle Management
  As an operator
  I want to activate or cancel a rental via the lifecycle endpoint
  So that rental status transitions are validated and side-effects are triggered correctly

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
      | BICYCLE | Bicycle | Two-wheeled |
      | SCOOTER | Scooter | Scooter     |
      | HELMET  | Helmet  | Helmet      |
      | OTHER   | Other   | Other       |
    And the following equipment records exist in db
      | id | serialNumber | uid            | status    | type    | model   | conditionNotes | condition |
      | 1  | EQ-001       | BIKE-001       | AVAILABLE | BICYCLE | Model A | Good           | GOOD      |
      | 2  | EQ-002       | E-BIKE-001     | AVAILABLE | SCOOTER | Model B | Excellent      | GOOD      |
      | 3  | EQ-003       | HELM-ADULT-001 | AVAILABLE | HELMET  | Model B | Excellent      | GOOD      |
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
    And the pricing params list for tariff request is
      | tariffId | pricingType       | firstHourPrice | hourlyDiscount | minimumHourlyPrice | hourlyPrice | dailyPrice | overtimeHourlyPrice | issuanceFee | minimumDurationMinutes | minimumDurationSurcharge | price |
      | 10       | DEGRESSIVE_HOURLY | 9.00           | 2.00           | 1.00               |             |            |                     |             | 30                     | 1.00                     |       |
      | 11       | FLAT_FEE          |                |                |                    |             |            |                     | 1.00        |                        |                          |       |
      | 12       | FLAT_HOURLY       |                |                |                    | 15.00       |            |                     |             | 30                     | 1.00                     |       |
    And the following tariff v2 records exist in db
      | id | name                   | description               | equipmentType | pricingType       | status | validFrom  | validTo |
      | 10 | Degressive Hourly Bike | Bicycle degressive hourly | BICYCLE       | DEGRESSIVE_HOURLY | ACTIVE | 2026-01-01 |         |
      | 11 | Flat Fee Helmet        | Helmet flat fee           | HELMET        | FLAT_FEE          | ACTIVE | 2026-01-01 |         |
      | 12 | Flat Hourly Scooter    | Scooter flat hourly rate  | SCOOTER       | FLAT_HOURLY       | ACTIVE | 2026-01-01 |         |

  Scenario: Activate a DRAFT rental — status becomes ACTIVE, hold placed, event published
    Given now is "2026-05-01T10:00:00"
    And a single rental exists in the database with the following data
      | id | customerId | status | plannedDuration | createdAt           | updatedAt           |
      | 1  | CUS1       | DRAFT  | 120             | 2026-04-28T09:00:00 | 2026-04-28T09:00:00 |
    And rental equipments exist in the database with the following data
      | rentalId | equipmentId | equipmentUid | equipmentType | tariffId | status   | startedAt           | expectedReturnAt    | estimatedCost | createdAt           | updatedAt           |
      | 1        | 1           | BIKE-001     | BICYCLE       | 10       | ASSIGNED | 2026-04-28T09:00:00 | 2026-04-28T11:00:00 | 16.00         | 2026-04-28T09:00:00 | 2026-04-28T09:00:00 |
    And the lifecycle request is
      | status | operatorId |
      | ACTIVE | OP1        |
    When a PATCH request has been made to "/api/rentals/{requestedObjectId}/lifecycles" endpoint with context
    Then the response status is 200
    #    rental module
    And rental was persisted in database
      | customerId | status |
      | CUS1       | ACTIVE |
    And rental equipments were persisted in database
      | equipmentUid | status |
      | BIKE-001     | ACTIVE |
    #    finance module
    And the following sub-ledger records were persisted in db
      | id     | accountId | ledgerType      | balance |
      | L_C_W1 | ACC1      | CUSTOMER_WALLET | 284.00  |
      | L_C_H1 | ACC1      | CUSTOMER_HOLD   | 16.00   |
    #    events
    And the following rental started event was published
      | customerId | startedAt |
      | CUS1       | now()     |

  Scenario: Attempt to activate a DRAFT rental with insufficient wallet balance
    Given a single rental exists in the database with the following data
      | id | customerId | status | plannedDuration | createdAt           | updatedAt           |
      | 1  | CUS1       | DRAFT  | 120             | 2026-04-28T09:00:00 | 2026-04-28T09:00:00 |
    And rental equipments exist in the database with the following data
      | rentalId | equipmentId | equipmentUid | equipmentType | tariffId | status   | startedAt           | expectedReturnAt    | estimatedCost | createdAt           | updatedAt           |
      | 1        | 1           | BIKE-001     | BICYCLE       | 10       | ASSIGNED | 2026-04-28T09:00:00 | 2026-04-28T11:00:00 | 16.00         | 2026-04-28T09:00:00 | 2026-04-28T09:00:00 |
    And the following sub-ledger records exist in db
      | id     | accountId | ledgerType      | balance | version | createdAt            | updatedAt            |
      | L_C_W3 | ACC1      | CUSTOMER_WALLET | 5.00    | 5       | 2026-04-28T09:00:00Z | 2026-04-28T09:00:00Z |
    And the lifecycle request is
      | status | operatorId |
      | ACTIVE | OP1        |
    When a PATCH request has been made to "/api/rentals/{requestedObjectId}/lifecycles" endpoint with context
    Then the response status is 422
    And the response contains
      | path        | value                                                               |
      | $.title     | Insufficient funds                                                  |
      | $.detail    | Insufficient wallet balance. Available: 5.00, requested deduction: 16.00 |
      | $.errorCode | rental.insufficient_funds                                           |

  Scenario Outline: Cancel a <rentalStatus> rental without hold — equipment RETURNED, hold balance unchanged
    Given a single rental exists in the database with the following data
      | id | customerId | status         | plannedDuration | createdAt           | updatedAt           |
      | 1  | CUS1       | <rentalStatus> | 120             | 2026-04-28T09:00:00 | 2026-04-28T09:00:00 |
    And rental equipments exist in the database with the following data
      | rentalId | equipmentId | equipmentUid | equipmentType | tariffId | status            | startedAt           | expectedReturnAt    | estimatedCost | createdAt           | updatedAt           |
      | 1        | 1           | BIKE-001     | BICYCLE       | 10       | <equipmentStatus> | 2026-04-28T09:00:00 | 2026-04-28T11:00:00 | 16.00         | 2026-04-28T09:00:00 | 2026-04-28T09:00:00 |
    And the lifecycle request is
      | status    | operatorId |
      | CANCELLED | OP1        |
    When a PATCH request has been made to "/api/rentals/{requestedObjectId}/lifecycles" endpoint with context
    Then the response status is 200
    #    rental module
    And rental was persisted in database
      | customerId | status    |
      | CUS1       | CANCELLED |
    And rental equipments were persisted in database
      | equipmentUid | status   |
      | BIKE-001     | RETURNED |
    #    finance module — no hold was present, CUSTOMER_HOLD balance must remain 0.00
    And the following sub-ledger records were persisted in db
      | id     | accountId | ledgerType    | balance |
      | L_C_H1 | ACC1      | CUSTOMER_HOLD | 0.00    |
    #    events
    And the following rental cancelled event was published
      | customerId |
      | CUS1       |
    Examples:
      | rentalStatus | equipmentStatus |
      | DRAFT        | ASSIGNED        |
      | ACTIVE       | ACTIVE          |

  Scenario: Cancel an ACTIVE rental with hold — hold released, equipment RETURNED, event published
    Given a single rental exists in the database with the following data
      | id | customerId | status | plannedDuration | createdAt           | updatedAt           |
      | 1  | CUS1       | ACTIVE | 120             | 2026-04-28T09:00:00 | 2026-04-28T10:00:00 |
    And rental equipments exist in the database with the following data
      | rentalId | equipmentId | equipmentUid | equipmentType | tariffId | status | startedAt           | expectedReturnAt    | estimatedCost | createdAt           | updatedAt           |
      | 1        | 1           | BIKE-001     | BICYCLE       | 10       | ACTIVE | 2026-04-28T10:00:00 | 2026-04-28T12:00:00 | 16.00         | 2026-04-28T09:00:00 | 2026-04-28T10:00:00 |
    And the following sub-ledger records exist in db
      | id     | accountId | ledgerType    | balance | version | createdAt            | updatedAt            |
      | L_C_H3 | ACC1      | CUSTOMER_HOLD | 16.00   | 3       | 2026-04-28T10:00:00Z | 2026-04-28T10:00:00Z |
    And the following transaction records exist in db
      | id  | type | paymentMethod | amount | customerId | operatorId | sourceType | sourceId | recordedAt          | idempotencyKey |
      | TX2 | HOLD | CASH          | 16.00  | CUS1       | OP1        | RENTAL     | 1        | 2026-04-28T10:00:00 | IDK2           |
    And the lifecycle request is
      | status    | operatorId |
      | CANCELLED | OP1        |
    When a PATCH request has been made to "/api/rentals/{requestedObjectId}/lifecycles" endpoint with context
    Then the response status is 200
    #    rental module
    And rental was persisted in database
      | customerId | status    |
      | CUS1       | CANCELLED |
    And rental equipments were persisted in database
      | equipmentUid | status   |
      | BIKE-001     | RETURNED |
    #    finance module — hold was placed, assert it has been released (balance 16.00 → 0.00)
    And the following sub-ledger records were persisted in db
      | id     | accountId | ledgerType    | balance |
      | L_C_H3 | ACC1      | CUSTOMER_HOLD | 0.00    |
    #    events
    And the following rental cancelled event was published
      | customerId |
      | CUS1       |
```

## 3. Notes

* `Background:` is intentionally identical to `rental.feature` to avoid shared state coupling
  between feature files.
* The "insufficient balance" scenario inserts a second `CUSTOMER_WALLET` sub-ledger row (`L_C_W3`
  with balance `5.00`) for ACC1 on top of the Background rows, overriding the effective available
  balance. It relies on the `InsertableSubLedgerRepository` upsert semantics that replace the
  existing row by ID.
* The "Activate" scenario asserts sub-ledger state after activation: wallet decreases by the
  estimated cost (`300.00 → 284.00`), hold increases by the same amount (`0.00 → 16.00`), using
  the existing `the following sub-ledger records were persisted in db` step.
* The cancel **Scenario Outline** covers two structurally identical cases — DRAFT cancellation
  and ACTIVE cancellation without a hold (special tariff applied). Both assert that the
  `CUSTOMER_HOLD` sub-ledger balance remains `0.00`, confirming no hold release was attempted.
  The `<equipmentStatus>` column reflects the actual DB state: `ASSIGNED` for DRAFT, `ACTIVE`
  for an already-started rental.
* The **Cancel ACTIVE with hold** scenario is a separate `Scenario` (not an outline row) because
  it requires additional seeding steps (hold sub-ledger + HOLD transaction) that cannot be
  conditionally included in an outline. It asserts that `CUSTOMER_HOLD` balance drops from
  `16.00` to `0.00`, confirming `FinanceFacade.releaseHold` was executed. This assertion
  requires FR-04's full `releaseHold` implementation to pass.
* The `rental equipments were persisted in database` step allows specifying only a subset of
  columns (`equipmentUid`, `status`); null columns are skipped in the assertion.

## 4. Validation Steps

skip
