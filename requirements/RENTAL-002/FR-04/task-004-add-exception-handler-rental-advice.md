# Task 004: Add `EquipmentOccupiedException` Handler to `RentalRestControllerAdvice`

> **Applied Skill:** `d:\Projects\private\bikerent\.github\instructions\springboot.instructions.md` —
`@ExceptionHandler`
> returning `ProblemDetail` with `correlationId`, `errorCode`, and a custom `unavailableIds` extra property; HTTP 409
> Conflict; follows the exact handler pattern already used in `RentalRestControllerAdvice` for `HoldRequiredException`.

## 1. Objective

Add a new `@ExceptionHandler` method for `EquipmentOccupiedException` to `RentalRestControllerAdvice`. The handler
returns HTTP `409 Conflict` with a `ProblemDetail` that includes `errorCode: "rental.equipment.not_available"`,
`correlationId`, and `unavailableIds` (the `Set<Long>` from the exception).

## 2. File to Modify / Create

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/rental/web/error/RentalRestControllerAdvice.java`
* **Action:** Modify Existing File

## 3. Code Implementation

**Imports Required:**

Add the following import to the existing import block in `RentalRestControllerAdvice.java`:

```java
import com.github.jenkaby.bikerental.rental.domain.exception.EquipmentOccupiedException;
```

> The wildcard import `import com.github.jenkaby.bikerental.rental.domain.exception.*;` already covers all exceptions in
> that package — confirm whether this specific class is already covered by it. If a wildcard is present, no additional
> import is needed.

**Code to Add/Replace:**

* **Location:** Inside `RentalRestControllerAdvice`, immediately **before** the `private String resolveCorrelationId()`
  method at the very end of the class.
* **Snippet:**

```java
    @ExceptionHandler(EquipmentOccupiedException.class)
    public ResponseEntity<ProblemDetail> handleEquipmentOccupied(EquipmentOccupiedException ex) {
        var correlationId = resolveCorrelationId();
        log.warn("[correlationId={}] Equipment occupied: {}", correlationId, ex.getMessage());
        var problem = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        problem.setTitle("Equipment not available");
        problem.setDetail(ex.getMessage());
        problem.setProperty(ProblemDetailField.CORRELATION_ID, correlationId);
        problem.setProperty(ProblemDetailField.ERROR_CODE, ErrorCodes.EQUIPMENT_NOT_AVAILABLE);
        problem.setProperty("unavailableIds", ex.getDetails().unavailableIds());
        return ResponseEntity.of(problem).build();
    }
```

> **Key rules:**
> - `"unavailableIds"` is a plain string property key (not in `ProblemDetailField` — that constant does not exist yet;
    > do not create it).
> - `ErrorCodes.EQUIPMENT_NOT_AVAILABLE` must be available (Task 001 of this FR).
> - `ex.getDetails().unavailableIds()` returns the `Set<Long>` from
    `EquipmentOccupiedException.OccupiedEquipmentDetails`.
> - The wildcard import `com.github.jenkaby.bikerental.rental.domain.exception.*` at the top of the file already
    > covers `EquipmentOccupiedException` — no additional import line needed.

## 4. Validation Steps

Execute the following commands to ensure this task was successful. Do NOT run the full application server.

```bash
./gradlew :service:compileJava
```
