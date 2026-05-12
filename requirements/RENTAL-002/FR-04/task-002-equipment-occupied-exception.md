# Task 002: Create `EquipmentOccupiedException` Domain Exception

> **Applied Skill:** `d:\Projects\private\bikerent\.github\instructions\java.instructions.md` — Domain exception
> extending `BikeRentalException`; follows the exact pattern of `HoldRequiredException` in
> `rental/domain/exception/` (inner `record` for details, `getDetails()` convenience method, `@Getter` from Lombok).

## 1. Objective

Create `EquipmentOccupiedException` in `rental/domain/exception/` carrying a `Set<Long>` of unavailable equipment IDs.
It uses the new `ErrorCodes.EQUIPMENT_NOT_AVAILABLE` constant (Task 001). The existing
`EquipmentNotAvailableException` in shared is NOT used — it carries `EquipmentDetails(identifier, status)` which is not
compatible with a set of `Long` IDs.

## 2. File to Modify / Create

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/rental/domain/exception/EquipmentOccupiedException.java`
* **Action:** Create New File

## 3. Code Implementation

**Imports Required:** See snippet below.

**Code to Add/Replace:**

* **Location:** New file — entire file content below.
* **Snippet:**

```java
package com.github.jenkaby.bikerental.rental.domain.exception;

import com.github.jenkaby.bikerental.shared.exception.BikeRentalException;
import com.github.jenkaby.bikerental.shared.web.advice.ErrorCodes;
import lombok.Getter;

import java.util.Set;

@Getter
public class EquipmentOccupiedException extends BikeRentalException {

    private static final String MESSAGE = "Requested equipment is already occupied in an active or assigned rental";

    public EquipmentOccupiedException(Set<Long> unavailableIds) {
        super(MESSAGE, ErrorCodes.EQUIPMENT_NOT_AVAILABLE, new OccupiedEquipmentDetails(unavailableIds));
    }

    public OccupiedEquipmentDetails getDetails() {
        return getParams()
                .map(d -> (OccupiedEquipmentDetails) d)
                .orElseThrow(() -> new IllegalArgumentException("Expected OccupiedEquipmentDetails in exception parameters"));
    }

    public record OccupiedEquipmentDetails(Set<Long> unavailableIds) {
    }
}
```

> **Key rules (matching `HoldRequiredException` pattern exactly):**
> - `@Getter` is on the class (Lombok generates getters for `errorCode` and `params` from the superclass).
> - `getDetails()` casts `getParams()` to the inner record — `getParams()` returns `Optional<Object>`.
> - The inner `record OccupiedEquipmentDetails` holds `Set<Long> unavailableIds` which is serialised directly into the
    > `ProblemDetail` as the `unavailableIds` JSON array.

## 4. Validation Steps

Execute the following commands to ensure this task was successful. Do NOT run the full application server.

```bash
./gradlew :service:compileJava
```
