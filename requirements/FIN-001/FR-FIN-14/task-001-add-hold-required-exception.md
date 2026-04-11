# Task 001: Add `HoldRequiredException` to Rental Domain

> **Applied Skill:** `java.instructions.md` — Java Records, `BikeRentalException` pattern followed by all existing
> domain exceptions in the rental module.

## 1. Objective

Create the `HoldRequiredException` domain exception in the Rental module. It is thrown by `UpdateRentalService` when a
draft rental has no `HOLD` journal entry and activation is attempted.

## 2. File to Modify / Create

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/rental/domain/exception/HoldRequiredException.java`
* **Action:** Create New File

## 3. Code Implementation

**Imports Required:**

```java
package com.github.jenkaby.bikerental.rental.domain.exception;

import com.github.jenkaby.bikerental.shared.exception.BikeRentalException;
import lombok.Getter;
```

**Code to Add/Replace:**

* **Location:** New file — full class content.

```java
package com.github.jenkaby.bikerental.rental.domain.exception;

import com.github.jenkaby.bikerental.shared.exception.BikeRentalException;
import lombok.Getter;

@Getter
public class HoldRequiredException extends BikeRentalException {

    public static final String ERROR_CODE = "rental.hold.required";

    private static final String MESSAGE = "A fund hold must exist before the rental can be activated";

    public HoldRequiredException(Long rentalId) {
        super(MESSAGE, ERROR_CODE, new HoldRequiredDetails(rentalId));
    }

    public HoldRequiredDetails getDetails() {
        return getParams()
                .map(d -> (HoldRequiredDetails) d)
                .orElseThrow(() -> new IllegalArgumentException("Expected HoldRequiredDetails in exception parameters"));
    }

    public record HoldRequiredDetails(Long rentalId) {
    }
}
```

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```
