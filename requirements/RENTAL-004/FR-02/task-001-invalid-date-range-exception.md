# Task 001: Create InvalidDateRangeException Domain Exception

> **Applied Skill:** `java.instructions.md` — domain exceptions extend BikeRentalException;
> `springboot.instructions.md` — error code constants pattern

## 1. Objective

Create a new unchecked domain exception `InvalidDateRangeException` in the rental module's
exception package. It is thrown when `from` is after `to`. Follows the same structure as
`InvalidRentalStatusException`: extends `BikeRentalException`, carries an `errorCode` constant,
and includes both date values in the detail message.

## 2. File to Modify / Create

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/rental/domain/exception/InvalidDateRangeException.java`
* **Action:** Create New File

## 3. Code Implementation

**Imports Required:**

```java
import com.github.jenkaby.bikerental.shared.exception.BikeRentalException;
import java.time.LocalDate;
```

**Code to Add/Replace:**

* **Location:** New file — replace entire content.

```java
package com.github.jenkaby.bikerental.rental.domain.exception;

import com.github.jenkaby.bikerental.shared.exception.BikeRentalException;

import java.time.LocalDate;

public class InvalidDateRangeException extends BikeRentalException {

    public static final String ERROR_CODE = "rental.date_range.invalid";

    private static final String MESSAGE_TEMPLATE = "'from' date (%s) must not be after 'to' date (%s)";

    public InvalidDateRangeException(LocalDate from, LocalDate to) {
        super(MESSAGE_TEMPLATE.formatted(from, to), ERROR_CODE);
    }
}
```

## 4. Validation Steps

skip
