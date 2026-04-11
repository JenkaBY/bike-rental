# Task 003: Add `hasHold` to `FinanceFacade` Interface

> **Applied Skill:** `springboot.instructions.md` — Keep interfaces minimal and caller-focused; no implementation
> details leak through the facade contract.

## 1. Objective

Add the `hasHold(RentalRef rentalRef)` query method to the `FinanceFacade` interface so that `UpdateRentalService` can
replace the deprecated `hasPrepayment` guard call with the new hold-based guard.

## 2. File to Modify / Create

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/finance/FinanceFacade.java`
* **Action:** Modify Existing File

## 3. Code Implementation

**Imports Required:** None — no new imports needed.

**Code to Add/Replace:**

* **Location:** Inside the `FinanceFacade` interface, directly **after** the `holdFunds` method declaration.

**Before:**

```java
    HoldInfo holdFunds(CustomerRef customerRef, RentalRef rentalRef, Money plannedCost);

    SettlementInfo settleRental(@NonNull CustomerRef customerRef, @NonNull RentalRef rentalRef,
```

**After:**

```java
    HoldInfo holdFunds(CustomerRef customerRef, RentalRef rentalRef, Money plannedCost);

    boolean hasHold(RentalRef rentalRef);

    SettlementInfo settleRental(@NonNull CustomerRef customerRef, @NonNull RentalRef rentalRef,
```

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```

> The build will fail until `FinanceFacadeImpl` implements the new method (Task 004). That is expected at this stage if
> you check compile only — proceed directly to Task 004.
