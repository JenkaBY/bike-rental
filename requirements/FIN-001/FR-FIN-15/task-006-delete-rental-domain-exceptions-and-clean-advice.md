# Task 006: Delete Rental Domain Exceptions and Clean RentalRestControllerAdvice

> **Applied Skill:** No dedicated skill file — follows error-handling conventions in `AGENTS.md`
> (`@RestControllerAdvice` module-scoped handlers).

## 1. Objective

Delete `PrepaymentRequiredException` and `InsufficientPrepaymentException` from the Rental domain exception package,
and remove the two corresponding `@ExceptionHandler` methods from `RentalRestControllerAdvice`. Both operations
**must** be performed together: the advice class directly references the exception types in method signatures, so
deleting the exceptions without cleaning the advice (or vice-versa) would cause a compile error.

---

## 2. Files to Modify / Create

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/rental/domain/exception/PrepaymentRequiredException.java`
* **Action:** Delete file entirely.

---

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/rental/domain/exception/InsufficientPrepaymentException.java`
* **Action:** Delete file entirely.

---

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/rental/web/error/RentalRestControllerAdvice.java`
* **Action:** Modify existing file — remove two `@ExceptionHandler` methods.

---

## 3. Code Implementation

### 3.1 Delete exception classes

Delete both exception files listed in Section 2. No replacements are needed.

---

### 3.2 Modify `RentalRestControllerAdvice.java` — remove two handlers

> **Note:** `RentalRestControllerAdvice` uses the wildcard import
> `import com.github.jenkaby.bikerental.rental.domain.exception.*;` — the import line itself does **not** need to
> change; simply removing the two methods is sufficient.

**Step A — Remove the `handlePrepaymentRequired` method.** Find and remove the following block:

```java
    @ExceptionHandler(PrepaymentRequiredException.class)
    public ResponseEntity<ProblemDetail> handlePrepaymentRequired(PrepaymentRequiredException ex) {
        var correlationId = resolveCorrelationId();
        log.warn("[correlationId={}] Prepayment required for rental {}: {}", correlationId, ex.getDetails().rentalId(), ex.getMessage());
        var problem = ProblemDetail.forStatus(HttpStatus.UNPROCESSABLE_CONTENT);
        problem.setTitle("Prepayment required");
        problem.setDetail(ex.getMessage());
        problem.setProperty(ProblemDetailField.CORRELATION_ID, correlationId);
        problem.setProperty(ProblemDetailField.ERROR_CODE, ex.getErrorCode());
        return ResponseEntity.of(problem).build();
    }
```

**Step B — Remove the `handleInsufficientPrepayment` method.** Find and remove the following block:

```java
    @ExceptionHandler(InsufficientPrepaymentException.class)
    public ResponseEntity<ProblemDetail> handleInsufficientPrepayment(InsufficientPrepaymentException ex) {
        var correlationId = resolveCorrelationId();
        log.warn("[correlationId={}] Insufficient prepayment for rental {}: {}", correlationId, ex.getDetails().rentalId(), ex.getMessage());
        var problem = ProblemDetail.forStatus(HttpStatus.UNPROCESSABLE_CONTENT);
        problem.setTitle("Insufficient prepayment");
        problem.setDetail(ex.getMessage());
        problem.setProperty(ProblemDetailField.CORRELATION_ID, correlationId);
        problem.setProperty(ProblemDetailField.ERROR_CODE, ex.getErrorCode());
        return ResponseEntity.of(problem).build();
    }
```

---

## 4. Validation Steps

```bash
./gradlew :service:compileJava "-Dspring.profiles.active=test"
```

Expected result: `BUILD SUCCESSFUL` with zero compilation errors.
