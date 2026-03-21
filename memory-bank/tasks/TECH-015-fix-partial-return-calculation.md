# [TECH-015] - Fix Partial Equipment Return Calculation Logic

**Status:** Pending  
**Added:** 2026-03-17  
**Updated:** 2026-03-17

## Original Request

The `Scenario Outline: Partial return - return one equipment from multi-equipment rental` in
`component-test/src/test/resources/features/rental/rental-return.feature` has incorrect expected values marked with the
comment `# FIXME wrong calculation logic`. The scenario should cover the correct return flow but currently has wrong
`additionalPayment` expectations for both the first partial and the final return.

## Thought Process

### Root Cause Analysis

The `ReturnEquipmentService` calculates `additionalPayment` as:

```java
Money toPay = totalCost.subtract(paymentsTotalAmount);
```

Where:

- `totalCost` = cost of the **currently returned equipment only**
- `paymentsTotalAmount` = **all payments ever made** for the rental

This formula is correct only for a **full** return (when all equipment is returned at once). For a **partial** return,
it produces wrong results because it ignores the estimated cost of the equipment that is still active.

### Incorrect Expectations in the Feature File

**Setup:** rental with 2 bikes, each `estimatedCost = 200.00`, prepayment = `400.00`, `plannedDuration = 120 min`,
`startedAt = 08:00`.

#### Step 1 — First partial return: BIKE-001 at `now = 10:00` (exactly 120 min, on time)

| Value               | Current (wrong) | Expected (correct) |
|---------------------|-----------------|--------------------|
| `additionalPayment` | `-200.00`       | `0`                |

**Why current is wrong:**

```
toPay = cost(BIKE-001=200) - allPayments(400) = -200
```

BIKE-002 is still ACTIVE with `estimatedCost = 200`. The prepayment of 400 covers both bikes precisely.
No refund and no extra charge is due yet.

**Correct formula for partial return:**

```
toPay = cost(returned) + remainingActiveEstimatedCost - allPayments
      = 200 + 200 - 400 = 0
```

#### Step 2 — Final return: BIKE-002 at `nowReturn = 11:00` (180 min, 60 min overtime)

With period-multiplier pricing (`hourPrice = 100`):

- `actualMinutes = 180` → `periods = ceil(180/60) = 3` → `baseCost = 300.00`, `overtimeCost = 0.00` (period
  pricing covers all time)

| Value               | Current (wrong) | Expected (correct) |
|---------------------|-----------------|--------------------|
| `additionalPayment` | `-100.00`       | `100.00`           |

**Why current is wrong:**

```
toPay = cost(BIKE-002=300) - allPayments(400) = -100
```

Treats only BIKE-002 against full prepayment, ignoring that BIKE-001 already consumed 200 of it.

**Correct formula for final return:**

```
toPay = sum(all returned finalCosts) - allPayments
      = (200 [BIKE-001] + 300 [BIKE-002]) - 400 = 100
```

### Fix Required in `ReturnEquipmentService`

The current `toPay` formula must be replaced with:

```java
// After marking equipmentsToReturn as RETURNED:

// 1. Sum final costs of equipment returned in PREVIOUS partial returns
Money previouslyReturnedCost = rental.getEquipments().stream()
        .filter(e -> e.getStatus() == RentalEquipmentStatus.RETURNED)
        .filter(e -> !equipmentsToReturn.contains(e))
        .map(RentalEquipment::getFinalCost)
        .reduce(Money.zero(), Money::add);

// 2. Estimated cost of equipment STILL ACTIVE after this return
Money remainingEstimatedCost = rental.getEquipments().stream()
        .filter(e -> e.getStatus() != RentalEquipmentStatus.RETURNED)
        .map(RentalEquipment::getEstimatedCost)
        .reduce(Money.zero(), Money::add);

// 3. Correct balance:
// toPay = previouslyReturned + currentReturned + remainingEstimated - allPayments
Money toPay = previouslyReturnedCost
        .add(totalCost)
        .add(remainingEstimatedCost)
        .subtract(paymentsTotalAmount);
```

For partial return `remainingEstimatedCost > 0` keeps the balance neutral (0 or close to 0 when prepayment was
exact). For final return `remainingEstimatedCost = 0`, so only real costs count.

### Feature File Corrections Needed

Both the incorrect expected values and missing/incomplete verification steps must be fixed:

1. First partial return:
    - `additionalPayment` → `0`
    - Add break-down cost assertion for BIKE-001: `baseCost=200.00, overtimeCost=0.00, totalCost=200.00,
      actualMinutes=120, billableMinutes=120, plannedMinutes=120, overtimeMinutes=0`

2. Second (final) return:
    - `additionalPayment` → `100.00` (positive, operator must collect payment)
    - Add `paymentMethod` and `paymentAmount` assertions (CASH, 100.00)
    - Verify rental status becomes `COMPLETED`
    - Verify BIKE-002 equipment status changes to `AVAILABLE`

3. Remove/replace the `# FIX me wrong calculation logic` comment.

## Implementation Plan

- [] 1. Fix `ReturnEquipmentService` — replace `toPay` formula with the correct partial/final return logic
- [] 2. Add helper method to `Rental` domain model — `sumFinalCostsOfReturnedEquipment()` and
  `sumEstimatedCostsOfActiveEquipment()` to keep domain logic encapsulated
- [] 3. Fix `rental-return.feature` — update expected `additionalPayment` values and add missing assertions
- [] 4. Verify existing scenarios still pass (full return, overtime, not found, invalid status)
- [] 5. Add/update unit tests for `ReturnEquipmentService` covering: full return, partial return (first pass), partial
  return (final pass)

## Progress Tracking

**Overall Status:** Not Started - 0%

### Subtasks

| ID   | Description                                                     | Status      | Updated    | Notes |
|------|-----------------------------------------------------------------|-------------|------------|-------|
| 15.1 | Fix `toPay` formula in `ReturnEquipmentService`                 | Not Started | 2026-03-17 |       |
| 15.2 | Add domain helper methods to `Rental` for cost summation        | Not Started | 2026-03-17 |       |
| 15.3 | Fix expected values in `rental-return.feature`                  | Not Started | 2026-03-17 |       |
| 15.4 | Add missing verification steps in partial-return scenario       | Not Started | 2026-03-17 |       |
| 15.5 | Unit tests for `ReturnEquipmentService` (partial + full return) | Not Started | 2026-03-17 |       |
| 15.6 | Run all tests with `test` profile and verify green              | Not Started | 2026-03-17 |       |

## Progress Log

### 2026-03-17

- Task created based on analysis of `rental-return.feature` partial-return scenario
- Root cause identified: `ReturnEquipmentService.toPay` formula ignores remaining active equipment costs
- `Rental.complete()` already handles the partial-return guard (only sets status `COMPLETED` when
  `allEquipmentReturned()` returns true) — no change needed there
- Period-multiplier pricing confirmed: `overtimeCost` is always `0.00`, base cost covers all periods using
  `ceil(billableMinutes / periodMinutes)` — BIKE-002 at 180 min = 3 * 100 = 300.00
- Forgiveness threshold is 7 minutes — does NOT apply to 60-min overtime in this scenario
- Correct expected values determined: first partial return `additionalPayment=0`, final return
  `additionalPayment=100.00`

