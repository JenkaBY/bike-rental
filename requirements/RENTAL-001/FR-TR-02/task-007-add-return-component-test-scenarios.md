# Task 007: Add Component Test Scenarios for Equipment Return (V2)

> **Applied Skill:** `spring-boot-java-cucumber` — Component tests cover happy paths only; reuse already existing step
> definitions; never assert on removed fields; verify DB state via JPA repository.

## 1. Objective

Add four Gherkin scenarios to `rental.feature` (under the existing `Feature: Rental Management`) that cover the
equipment-return happy paths defined in FR-TR-02:

1. Single item returned — standard V2 cost (exact 2h)
2. Multi-item final return — batch calculation with 10% discount
3. Partial return — first item only, rental stays ACTIVE
4. Final return with SPECIAL pricing

All four scenarios rely on the existing `Background:` block (tariff V2 records, customer CUS1 with wallet 300.00,
equipment IDs 1 and 3, account/sub-ledger records). Each scenario overrides only the state it needs.

## 2. File to Modify / Create

* **File Path:**
  `component-test/src/test/resources/features/rental/rental.feature`
* **Action:** Modify Existing File

## 3. Code Implementation

**Location:** Append the four scenarios at the very end of the file, after the last existing scenario
(`Create rental with discount — hold reflects discounted total`).

```gherkin
  # Equipment Return Scenarios (FR-TR-02)

  @ResetClock
  Scenario: Single equipment return — V2 cost applied at exact planned duration
    Given now is "2026-04-14T10:00:00"
    And a single rental exists in the database with the following data
      | id | customerId | status | plannedDuration | startedAt           | createdAt           | updatedAt           |
      | 5  | CUS1       | ACTIVE | 120             | 2026-04-14T10:00:00 | 2026-04-14T09:50:00 | 2026-04-14T10:00:00 |
    And rental equipment exists in the database with the following data
      | rentalId | equipmentId | equipmentUid | equipmentType | status | startedAt           | expectedReturnAt    | estimatedCost | createdAt           |
      | 5        | 1           | BIKE-001     | bicycle       | ACTIVE | 2026-04-14T10:00:00 | 2026-04-14T12:00:00 | 16.00         | 2026-04-14T10:00:00 |
    And the following sub-ledger records exist in db
      | id     | accountId | ledgerType    | balance | version | createdAt            | updatedAt            |
      | L_C_H1 | ACC1      | CUSTOMER_HOLD | 16.00   | 3       | 2026-03-27T00:00:00Z | 2026-04-14T10:00:00Z |
      | L_C_W1 | ACC1      | CUSTOMER_WALLET | 284.00 | 3      | 2026-03-27T00:00:00Z | 2026-04-14T10:00:00Z |
    And the following transaction records exist in db
      | id   | type | paymentMethod     | amount | customerId | operatorId | sourceType | sourceId | recordedAt          | idempotencyKey |
      | TX_H | HOLD | INTERNAL_TRANSFER | 16.00  | CUS1       | OP1        | RENTAL     | 5        | 2026-04-14T10:00:00 | IDK_H5         |
    And the following equipment records exist in db
      | id | serialNumber | uid      | status | type    | model   | condition |
      | 1  | EQ-001       | BIKE-001 | RENTED | bicycle | Model A | Good      |
    And the return equipment request is
      | rentalId | equipmentIds | paymentMethod | operatorId |
      |          | 1            | CASH          | OP1        |
    And now is "2026-04-14T12:00:00"
    When a POST request has been made to "/api/rentals/return" endpoint
    Then the response status is 200
    And the rental return response contains rental
      | status    | finalCost |
      | COMPLETED | 16.00     |
    And the rental return response does contain settlement info
    And rental equipment was persisted in database
      | rentalId | equipmentId | equipmentUid | status   | finalCost |
      | 5        | 1           | BIKE-001     | RETURNED | 16.00     |

  @ResetClock
  Scenario: Multi-item final return — batch V2 calculation with 10% discount
    Given now is "2026-04-14T10:00:00"
    And a single rental exists in the database with the following data
      | id | customerId | status | plannedDuration | discountPercent | startedAt           | createdAt           | updatedAt           |
      | 6  | CUS1       | ACTIVE | 120             | 10              | 2026-04-14T10:00:00 | 2026-04-14T09:50:00 | 2026-04-14T10:00:00 |
    And rental equipments exist in the database with the following data
      | rentalId | equipmentId | equipmentUid   | equipmentType | status | startedAt           | expectedReturnAt    | estimatedCost | createdAt           |
      | 6        | 1           | BIKE-001       | bicycle       | ACTIVE | 2026-04-14T10:00:00 | 2026-04-14T12:00:00 | 16.00         | 2026-04-14T10:00:00 |
      | 6        | 3           | HELM-ADULT-001 | helmet        | ACTIVE | 2026-04-14T10:00:00 | 2026-04-14T12:00:00 | 1.00          | 2026-04-14T10:00:00 |
    And the following sub-ledger records exist in db
      | id     | accountId | ledgerType    | balance | version | createdAt            | updatedAt            |
      | L_C_H1 | ACC1      | CUSTOMER_HOLD | 15.30   | 3       | 2026-03-27T00:00:00Z | 2026-04-14T10:00:00Z |
      | L_C_W1 | ACC1      | CUSTOMER_WALLET | 284.70 | 3      | 2026-03-27T00:00:00Z | 2026-04-14T10:00:00Z |
    And the following transaction records exist in db
      | id   | type | paymentMethod     | amount | customerId | operatorId | sourceType | sourceId | recordedAt          | idempotencyKey |
      | TX_H | HOLD | INTERNAL_TRANSFER | 15.30  | CUS1       | OP1        | RENTAL     | 6        | 2026-04-14T10:00:00 | IDK_H6         |
    And the following equipment records exist in db
      | id | serialNumber | uid            | status | type    | model   | condition |
      | 1  | EQ-001       | BIKE-001       | RENTED | bicycle | Model A | Good      |
      | 3  | EQ-003       | HELM-ADULT-001 | RENTED | helmet  | Model B | Excellent |
    And the return equipment request is
      | rentalId | equipmentIds | paymentMethod | operatorId |
      |          | 1,3          | CASH          | OP1        |
    And now is "2026-04-14T12:00:00"
    When a POST request has been made to "/api/rentals/return" endpoint
    Then the response status is 200
    And the rental return response contains rental
      | status    | finalCost |
      | COMPLETED | 15.30     |
    And the rental return response does contain settlement info
    And rental equipments were persisted in database
      | equipmentId | equipmentUid   | status   |
      | 1           | BIKE-001       | RETURNED |
      | 3           | HELM-ADULT-001 | RETURNED |

  @ResetClock
  Scenario: Partial return — first bicycle returned, rental remains ACTIVE
    Given now is "2026-04-14T10:00:00"
    And a single rental exists in the database with the following data
      | id | customerId | status | plannedDuration | startedAt           | createdAt           | updatedAt           |
      | 7  | CUS1       | ACTIVE | 120             | 2026-04-14T10:00:00 | 2026-04-14T09:50:00 | 2026-04-14T10:00:00 |
    And rental equipments exist in the database with the following data
      | rentalId | equipmentId | equipmentUid   | equipmentType | status | startedAt           | expectedReturnAt    | estimatedCost | createdAt           |
      | 7        | 1           | BIKE-001       | bicycle       | ACTIVE | 2026-04-14T10:00:00 | 2026-04-14T12:00:00 | 16.00         | 2026-04-14T10:00:00 |
      | 7        | 3           | HELM-ADULT-001 | helmet        | ACTIVE | 2026-04-14T10:00:00 | 2026-04-14T12:00:00 | 1.00          | 2026-04-14T10:00:00 |
    And the following equipment records exist in db
      | id | serialNumber | uid            | status | type    | model   | condition |
      | 1  | EQ-001       | BIKE-001       | RENTED | bicycle | Model A | Good      |
      | 3  | EQ-003       | HELM-ADULT-001 | RENTED | helmet  | Model B | Excellent |
    And the return equipment request is
      | rentalId | equipmentIds | paymentMethod | operatorId |
      |          | 1            | CASH          | OP1        |
    And now is "2026-04-14T12:00:00"
    When a POST request has been made to "/api/rentals/return" endpoint
    Then the response status is 200
    And the rental return response contains rental
      | status |
      | ACTIVE |
    And the rental return response {booleanDo} contain settlement info
      | false |
    And rental equipment was persisted in database
      | rentalId | equipmentId | equipmentUid | status   | finalCost |
      | 7        | 1           | BIKE-001     | RETURNED | 16.00     |

  @ResetClock
  Scenario: Final return with SPECIAL pricing — V2 uses flat special price
    Given now is "2026-04-14T10:00:00"
    And a single rental exists in the database with the following data
      | id | customerId | status | plannedDuration | specialTariffId | specialPrice | startedAt           | createdAt           | updatedAt           |
      | 8  | CUS1       | ACTIVE | 120             | 13              | 15.00        | 2026-04-14T10:00:00 | 2026-04-14T09:50:00 | 2026-04-14T10:00:00 |
    And rental equipment exists in the database with the following data
      | rentalId | equipmentId | equipmentUid | equipmentType | status | startedAt           | expectedReturnAt    | estimatedCost | createdAt           |
      | 8        | 1           | BIKE-001     | bicycle       | ACTIVE | 2026-04-14T10:00:00 | 2026-04-14T12:00:00 | 15.00         | 2026-04-14T10:00:00 |
    And the following sub-ledger records exist in db
      | id     | accountId | ledgerType    | balance | version | createdAt            | updatedAt            |
      | L_C_H1 | ACC1      | CUSTOMER_HOLD | 15.00   | 3       | 2026-03-27T00:00:00Z | 2026-04-14T10:00:00Z |
      | L_C_W1 | ACC1      | CUSTOMER_WALLET | 285.00 | 3      | 2026-03-27T00:00:00Z | 2026-04-14T10:00:00Z |
    And the following transaction records exist in db
      | id   | type | paymentMethod     | amount | customerId | operatorId | sourceType | sourceId | recordedAt          | idempotencyKey |
      | TX_H | HOLD | INTERNAL_TRANSFER | 15.00  | CUS1       | OP1        | RENTAL     | 8        | 2026-04-14T10:00:00 | IDK_H8         |
    And the following equipment records exist in db
      | id | serialNumber | uid      | status | type    | model   | condition |
      | 1  | EQ-001       | BIKE-001 | RENTED | bicycle | Model A | Good      |
    And the return equipment request is
      | rentalId | equipmentIds | paymentMethod | operatorId |
      |          | 1            | CASH          | OP1        |
    And now is "2026-04-14T12:00:00"
    When a POST request has been made to "/api/rentals/return" endpoint
    Then the response status is 200
    And the rental return response contains rental
      | status    | finalCost |
      | COMPLETED | 15.00     |
    And the rental return response does contain settlement info
    And rental equipment was persisted in database
      | rentalId | equipmentId | equipmentUid | status   | finalCost |
      | 8        | 1           | BIKE-001     | RETURNED | 15.00     |
```

### Notes for the implementing agent

1. **`@ResetClock`** annotation is required here because `ReturnEquipmentService` calls `LocalDateTime.now(clock)`.
   The `Given now is "..."` step sets that fixed clock time. The second `And now is "..."` just before the `When`
   moves the clock forward to the return moment. Ensure the test infrastructure supports two clock overrides
   in a single scenario; if not, derive `startedAt` from the return time minus the duration instead.

2. **`the rental return response {booleanDo} contain settlement info`** — the Scenario 3 step passes `false` as the
   Examples value. In Gherkin the step takes the boolean inline. Verify the existing step signature in
   `RentalReturnWebSteps` matches. Existing signature:
   `@Then("the rental return response {booleanDo} contain settlement info")`.
   The `false` value should map to `Boolean shouldContain = false`.

3. **Sub-ledger version** — the `version` column reflects the optimistic lock. Use version `3` to signal the record
   was already updated twice (deposit + hold). If the finance settlement code reads version and it clashes, bump to `4`.

4. **DB truncation hook** — verify that `rental_equipments` and `rentals` are in the list `DbSteps.TABLE_TO_TRUNCATE`
   so state is cleaned between scenarios. If `tariff_v2_params` or `sub_ledgers` are not in the list, add them.

5. **`RentalJpaEntityTransformer`** — confirm the transformer supports the `specialTariffId` and `specialPrice` columns
   (added in FR-TR-01). If not, extend the transformer before writing Scenario 4.

## 4. Validation Steps

```bash
./gradlew :component-test:test "-Dspring.profiles.active=test" --tests "Return*"
```

If no test class name matches, run all component tests:

```bash
./gradlew :component-test:test "-Dspring.profiles.active=test"
```
