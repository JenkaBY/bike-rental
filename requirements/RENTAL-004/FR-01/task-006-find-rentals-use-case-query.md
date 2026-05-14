# Task 006: Add from/to Date Fields to FindRentalsUseCase.FindRentalsQuery

> **Applied Skill:** `java.instructions.md` — immutable records, nullable fields;
> `springboot.instructions.md` — use-case interfaces as contracts

## 1. Objective

Add two optional `LocalDate` fields — `from` and `to` — to the `FindRentalsQuery` nested record
inside `FindRentalsUseCase`. These fields carry the raw date range supplied by the caller before
any boundary conversion.

## 2. File to Modify / Create

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/rental/application/usecase/FindRentalsUseCase.java`
* **Action:** Modify Existing File

## 3. Code Implementation

**Imports Required:**

Add:

```java
import org.jspecify.annotations.Nullable;
import java.time.LocalDate;
```

**Code to Add/Replace:**

* **Location:** Inside `FindRentalsQuery` record — add `from` and `to` as the last two components.

Replace:

```java
    record FindRentalsQuery(
            RentalStatus status,
            UUID customerId,
            String equipmentUid,
            PageRequest pageRequest
    ) {
    }
```

With:

```java
    record FindRentalsQuery(
            @Nullable RentalStatus status,
            @Nullable UUID customerId,
            @Nullable String equipmentUid,
            PageRequest pageRequest,
            @Nullable LocalDate from,
            @Nullable LocalDate to
    ) {
    }
```

## 4. Validation Steps

skip
