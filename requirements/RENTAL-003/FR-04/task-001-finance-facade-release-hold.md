# Task 001: Add releaseHold to FinanceFacade

> **Applied Skill:** `springboot.instructions.md` — cross-module Facade contracts

## 1. Objective

Add a `releaseHold(RentalRef rentalRef)` method to the `FinanceFacade` interface so
`CancelRentalService` can return the held funds to the customer's account when an ACTIVE rental
is cancelled.

## 2. File to Modify / Create

### File A — FinanceFacade Interface

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/finance/FinanceFacade.java`
* **Action:** Modify Existing File

### File B — FinanceFacadeImpl

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/finance/FinanceFacadeImpl.java`
* **Action:** Modify Existing File

## 3. Code Implementation

**Imports Required:**

```java
import org.jspecify.annotations.NonNull;
```

> Already imported in this file.

**Code to Add — File A (`FinanceFacade`):**

* **Location:** After the `boolean hasHold(RentalRef rentalRef);` method declaration.

```java
    void releaseHold(@NonNull RentalRef rentalRef);
```

**Code to Add — File B (`FinanceFacadeImpl`):**

* **Location:** After the closing `}` of the `hasHold` method, before the closing `}` of the class.

```java
    @Override
    public void releaseHold(@NonNull RentalRef rentalRef) {
        // TODO: implement hold release logic
    }
```

## 4. Validation Steps

skip