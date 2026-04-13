# Task 012: Update `rental.feature` — V2 Tariff Background + Fix Existing Scenarios

> **Applied Skills:**  
> `.github/skills/spring-boot-java-cucumber/SKILL.md` — Cucumber feature file conventions, Gherkin Background, DataTable

## 1. Objective

After Task 007 and 008 migrate `CreateRentalService` and `UpdateRentalService` to V2, the existing
`rental.feature` scenarios break because:

- The Background only has V1 tariff records; V2 needs its own tariff fixtures.
- Three scenarios hard-code V1 cost values and assert `tariffId` per equipment item (V2 does not
  populate `RentalEquipment.tariffId`).

This task adds V2 tariff records to the Background and fixes those three scenarios.

## 2. File to Modify

* **File Path:** `component-test/src/test/resources/features/rental/rental.feature`
* **Action:** Modify Existing File

---

## 3. V2 Tariff Pricing Reference

The new V2 tariffs use these IDs (chosen to avoid collision with V1 tariff IDs 1–4):

| V2 ID | Equipment Type | Pricing Type      | Key Params                                                                                              |
|-------|----------------|-------------------|---------------------------------------------------------------------------------------------------------|
| 10    | bicycle        | DEGRESSIVE_HOURLY | firstHourPrice=9.00, hourlyDiscount=2.00, minimumHourlyPrice=1.00, minDuration=30min, minSurcharge=1.00 |
| 11    | helmet         | FLAT_FEE          | issuanceFee=1.00                                                                                        |
| 12    | scooter        | FLAT_HOURLY       | hourlyPrice=15.00, minDuration=30min, minSurcharge=1.00                                                 |
| 13    | any            | SPECIAL           | price=0                                                                                                 |

**V2 cost values used in assertions:**

| Equipment | Duration | V2 Cost |
|-----------|----------|---------|
| bicycle   | 120 min  | 16.00   |
| helmet    | any      | 1.00    |
| scooter   | 60 min   | 15.00   |
| bicycle   | 60 min   | 9.00    |

---

## 4. Code Changes

### 4a — Add V2 tariff fixtures to Background

Locate the end of the Background block, just before the first empty line that separates Background from scenarios. The
last line of the Background currently ends with the `transaction records` table.

After the last `transaction records` table row (leaving the existing content intact), append the following
two new Background steps:

```gherkin
    And the pricing params list for tariff request is
      | tariffId | pricingType       | firstHourPrice | hourlyDiscount | minimumHourlyPrice | hourlyPrice | dailyPrice | overtimeHourlyPrice | issuanceFee | minimumDurationMinutes | minimumDurationSurcharge | price |
      | 10       | DEGRESSIVE_HOURLY | 9.00           | 2.00           | 1.00               |             |            |                     |             | 30                     | 1.00                     |       |
      | 11       | FLAT_FEE          |                |                |                    |             |            |                     | 1.00        |                        |                          |       |
      | 12       | FLAT_HOURLY       |                |                |                    | 15.00       |            |                     |             | 30                     | 1.00                     |       |
      | 13       | SPECIAL           |                |                |                    |             |            |                     |             |                        |                          | 0     |
    And the following tariff v2 records exist in db
      | id | name                    | description                 | equipmentType | pricingType       | status | validFrom  | validTo |
      | 10 | Degressive Hourly Bike  | Bicycle degressive hourly   | bicycle       | DEGRESSIVE_HOURLY | ACTIVE | 2026-01-01 |         |
      | 11 | Flat Fee Helmet         | Helmet flat fee             | helmet        | FLAT_FEE          | ACTIVE | 2026-01-01 |         |
      | 12 | Flat Hourly Scooter     | Scooter flat hourly rate    | scooter       | FLAT_HOURLY       | ACTIVE | 2026-01-01 |         |
      | 13 | Special Group Tariff    | Special pricing for groups  | any           | SPECIAL           | ACTIVE | 2025-01-01 |         |
```

> **Note:** `the pricing params list for tariff request is` stores params per tariffId in
> `ScenarioContext.pricingParamsContext`. `theFollowingTariffsV2Exist` in `TariffV2DbSteps` reads
> and sets them on the entity before insert. See `TariffV2DbSteps.theFollowingTariffsV2Exist`.

---

### 4b — Fix "Create rental with all required fields (tariff autoselect)"

**Before (current):**

```gherkin
    And the rental response only contains rental equipments
      | equipmentId    | equipmentUid   | status   | tariffId | estimatedCost | finalCost |
      | <equipmentId>  | BIKE-001       | ASSIGNED | 1        | 200.00        |           |
      | <equipmentId2> | HELM-ADULT-001 | ASSIGNED | 3        | 20.00         |           |
    #    rental module
    And rental equipments were persisted in database
      | equipmentId    | equipmentUid   | tariffId | status   | estimatedCost |
      | <equipmentId>  | BIKE-001       | 1        | ASSIGNED | 200.00        |
      | <equipmentId2> | HELM-ADULT-001 | 3        | ASSIGNED | 20.00         |
```

Also update the rental-level cost assertion (same scenario block):

```gherkin
    And the rental response only contains
      | customerId   | status | plannedDuration   | estimatedCost |
      | <customerId> | DRAFT  | <plannedDuration> | 220.00        |
```

**After:**

```gherkin
    And the rental response only contains
      | customerId   | status | plannedDuration   | estimatedCost |
      | <customerId> | DRAFT  | <plannedDuration> | 17.00         |
    And the rental response only contains rental equipments
      | equipmentId    | equipmentUid   | status   | estimatedCost |
      | <equipmentId>  | BIKE-001       | ASSIGNED | 16.00         |
      | <equipmentId2> | HELM-ADULT-001 | ASSIGNED | 1.00          |
    #    rental module
    And rental equipments were persisted in database
      | equipmentId    | equipmentUid   | status   | estimatedCost |
      | <equipmentId>  | BIKE-001       | ASSIGNED | 16.00         |
      | <equipmentId2> | HELM-ADULT-001 | ASSIGNED | 1.00          |
```

> `tariffId` is removed from both tables. Null-guard added by Task 011 means absence = not asserted.  
> Costs: bicycle 120min DEGRESSIVE_HOURLY = 16.00; helmet FLAT_FEE = 1.00; total = 17.00.

---

### 4c — Fix "Update rental - select equipment"

Find the assertions block inside `Scenario Outline: Update rental - select equipment`:

**Before:**

```gherkin
    Then the response status is 200
    And the rental response only contains
      | customerId   | status | plannedDuration   | estimatedCost |
      | <customerId> | DRAFT  | <plannedDuration> | 300.00        |
    And the rental response only contains rental equipments
      | equipmentId | equipmentUid | status   | tariffId | estimatedCost | finalCost |
      | 2           | E-BIKE-001   | ASSIGNED | 4        | 300.00        |           |
    And the following equipment records were persisted in db
```

Also:

```gherkin
    And rental equipment was persisted in database
      | rentalId   | equipmentId | equipmentUid | status   | estimatedCost | tariffId |
      | <rentalId> | 2           | E-BIKE-001   | ASSIGNED | 300.00        | 4        |
```

**After:**

```gherkin
    Then the response status is 200
    And the rental response only contains
      | customerId   | status | plannedDuration   | estimatedCost |
      | <customerId> | DRAFT  | <plannedDuration> | 15.00         |
    And the rental response only contains rental equipments
      | equipmentId | equipmentUid | status   | estimatedCost | finalCost |
      | 2           | E-BIKE-001   | ASSIGNED | 15.00         |           |
    And the following equipment records were persisted in db
```

```gherkin
    And rental equipment was persisted in database
      | rentalId   | equipmentId | equipmentUid | status   | estimatedCost |
      | <rentalId> | 2           | E-BIKE-001   | ASSIGNED | 15.00         |
```

> Cost: scooter 60min FLAT_HOURLY = 15.00.

---

### 4d — Fix "Update rental - select equipment twice"

Find the scenario `Scenario Outline: Update rental - select equipment twice`. It contains two
PATCH exchanges. Update both assertion blocks.

**First PATCH assertions — Before:**

```gherkin
    Then the response status is 200
    And the rental response only contains
      | customerId   | status | plannedDuration   | estimatedCost |
      | <customerId> | DRAFT  | <plannedDuration> | 300.00        |
    And the rental response only contains rental equipments
      | equipmentId | equipmentUid | status   | tariffId | estimatedCost | finalCost |
      | 2           | E-BIKE-001   | ASSIGNED | 4        | 300.00        |           |
    #    rental module
    And rental was persisted in database
      | customerId   | status | plannedDuration   |
      | <customerId> | DRAFT  | <plannedDuration> |
    And rental equipment was persisted in database
      | rentalId   | equipmentId | equipmentUid | status   | estimatedCost | tariffId |
      | <rentalId> | 2           | E-BIKE-001   | ASSIGNED | 300.00        | 4        |
```

**After:**

```gherkin
    Then the response status is 200
    And the rental response only contains
      | customerId   | status | plannedDuration   | estimatedCost |
      | <customerId> | DRAFT  | <plannedDuration> | 15.00         |
    And the rental response only contains rental equipments
      | equipmentId | equipmentUid | status   | estimatedCost | finalCost |
      | 2           | E-BIKE-001   | ASSIGNED | 15.00         |           |
    #    rental module
    And rental was persisted in database
      | customerId   | status | plannedDuration   |
      | <customerId> | DRAFT  | <plannedDuration> |
    And rental equipment was persisted in database
      | rentalId   | equipmentId | equipmentUid | status   | estimatedCost |
      | <rentalId> | 2           | E-BIKE-001   | ASSIGNED | 15.00         |
```

**Second PATCH assertions — Before:**

```gherkin
    Then the response status is 200
    And the rental response only contains
      | customerId   | status | plannedDuration   | estimatedCost |
      | <customerId> | DRAFT  | <plannedDuration> | 400.00        |
    And the rental response only contains rental equipments
      | equipmentId | equipmentUid | status   | tariffId | estimatedCost | finalCost |
      | 1           | BIKE-001     | ASSIGNED | 1        | 100.00        |           |
      | 2           | E-BIKE-001   | ASSIGNED | 4        | 300.00        |           |
```

**After:**

```gherkin
    Then the response status is 200
    And the rental response only contains
      | customerId   | status | plannedDuration   | estimatedCost |
      | <customerId> | DRAFT  | <plannedDuration> | 24.00         |
    And the rental response only contains rental equipments
      | equipmentId | equipmentUid | status   | estimatedCost | finalCost |
      | 1           | BIKE-001     | ASSIGNED | 9.00          |           |
      | 2           | E-BIKE-001   | ASSIGNED | 15.00         |           |
```

> Costs: scooter 60min = 15.00; bicycle 60min DEGRESSIVE_HOURLY = 9.00; total = 24.00.

---

### 4e — Note on "no suitable tariff found" scenario

The scenario `Create rental with auto-selected tariff when no suitable tariff found` (equipment type `other`)
should continue to pass because:

- V2 also throws `SuitableTariffNotFoundException` when no active tariff exists for the equipment type.
- `other` type has no V2 tariff in the Background.
- Verify the error message format matches the feature assertion after the V2 migration is deployed.
  If the message format differs, update `$.detail` in that scenario accordingly.

## 5. Validation Steps

```bash
./gradlew :component-test:test "-Dspring.profiles.active=test,docker" \
  --tests "RunComponentTests" \
  -Dcucumber.filter.tags="@RentalCreation or @RentalUpdate"
```

If tags are not applied, run the full component suite:

```bash
./gradlew :component-test:test "-Dspring.profiles.active=test,docker"
```
