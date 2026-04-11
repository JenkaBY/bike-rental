# Task 009: Add `handleInsufficientBalance` and `handleHoldRequired` to `RentalRestControllerAdvice`

> **Applied Skill:** `springboot.instructions.md` — Module-scoped `@RestControllerAdvice` handles exceptions for the
> rental module only; follows existing handler patterns in the same class.

## 1. Objective

Add two new `@ExceptionHandler` methods to `RentalRestControllerAdvice`:

1. `handleInsufficientBalance` — maps `InsufficientBalanceException` (thrown by the finance module through the module
   boundary) to `422 Unprocessable Entity` with `errorCode = INSUFFICIENT_FUNDS` and extra fields `availableBalance` and
   `requiredAmount`.
2. `handleHoldRequired` — maps `HoldRequiredException` to `409 Conflict` with `errorCode = HOLD_REQUIRED`.

## 2. File to Modify / Create

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/rental/web/error/RentalRestControllerAdvice.java`
* **Action:** Modify Existing File

## 3. Code Implementation

**Step A — Add imports:**

Add the following imports at the top of the file, after the existing imports:

```java
import com.github.jenkaby.bikerental.shared.exception.InsufficientBalanceException;
import com.github.jenkaby.bikerental.rental.domain.exception.HoldRequiredException;
import com.github.jenkaby.bikerental.shared.web.advice.ErrorCodes;
```

> Verify `ErrorCodes` is not already imported before adding it.

**Step B — Add the two new handler methods:**

* **Location:** Inside `RentalRestControllerAdvice`, directly **after** the existing `handleInvalidRentalUpdate` handler
  and **before** the existing `handlePrepaymentRequired` handler.

**Before:**

```java
    @ExceptionHandler(PrepaymentRequiredException.class)
    public ResponseEntity<ProblemDetail> handlePrepaymentRequired(PrepaymentRequiredException ex) {
```

**After (insert the two new handlers before the `PrepaymentRequiredException` handler):**

```java
    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<ProblemDetail> handleInsufficientBalance(InsufficientBalanceException ex) {
        var correlationId = resolveCorrelationId();
        log.warn("[correlationId={}] Insufficient balance for rental creation: {}", correlationId, ex.getMessage());
        var problem = ProblemDetail.forStatus(HttpStatus.UNPROCESSABLE_ENTITY);
        problem.setTitle("Insufficient funds");
        problem.setDetail(ex.getMessage());
        problem.setProperty(ProblemDetailField.CORRELATION_ID, correlationId);
        problem.setProperty(ProblemDetailField.ERROR_CODE, ErrorCodes.INSUFFICIENT_FUNDS);
        return ResponseEntity.of(problem).build();
    }

    @ExceptionHandler(HoldRequiredException.class)
    public ResponseEntity<ProblemDetail> handleHoldRequired(HoldRequiredException ex) {
        var correlationId = resolveCorrelationId();
        log.warn("[correlationId={}] Hold required for rental {}: {}", correlationId, ex.getDetails().rentalId(), ex.getMessage());
        var problem = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        problem.setTitle("Hold required");
        problem.setDetail(ex.getMessage());
        problem.setProperty(ProblemDetailField.CORRELATION_ID, correlationId);
        problem.setProperty(ProblemDetailField.ERROR_CODE, ErrorCodes.HOLD_REQUIRED);
        return ResponseEntity.of(problem).build();
    }

    @ExceptionHandler(PrepaymentRequiredException.class)
    public ResponseEntity<ProblemDetail> handlePrepaymentRequired(PrepaymentRequiredException ex) {
```

> **Note on HTTP status constant:** Check which constant is used by other handlers in this class. Existing handlers use
`HttpStatus.UNPROCESSABLE_CONTENT` (Spring 6 alias). Use the same constant that is already used in the file for 422
> responses instead of `HttpStatus.UNPROCESSABLE_ENTITY` to stay consistent. For 409, use `HttpStatus.CONFLICT`.

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```
