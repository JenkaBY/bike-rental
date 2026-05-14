# Task 002: Add from > to Guard to FindRentalsService

> **Applied Skill:** `springboot.instructions.md` — fail-fast pre-condition before I/O;
> `java.instructions.md` — guard clauses, no business logic in controllers

## 1. Objective

Add a pre-condition guard at the top of `FindRentalsService.execute()` that throws
`InvalidDateRangeException` when both `from` and `to` are non-null and `from` is strictly after
`to`. The guard runs before any filter construction or repository call.

## 2. File to Modify / Create

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/rental/application/service/FindRentalsService.java`
* **Action:** Modify Existing File

## 3. Code Implementation

**Imports Required:**

Add:

```java
import com.github.jenkaby.bikerental.rental.domain.exception.InvalidDateRangeException;
```

**Code to Add/Replace:**

* **Location:** Add the guard as the very first statement inside `execute()`, before the
  `RentalSearchFilter` construction added in FR-01 Task 007.

Replace:

```java
    @Override
    public Page<Rental> execute(FindRentalsQuery query) {
        var filter = new RentalSearchFilter(
```

With:

```java
    @Override
    public Page<Rental> execute(FindRentalsQuery query) {
        if (query.from() != null && query.to() != null && query.from().isAfter(query.to())) {
            throw new InvalidDateRangeException(query.from(), query.to());
        }
        var filter = new RentalSearchFilter(
```

## 4. Validation Steps

skip
