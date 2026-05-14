# Task 003: Add InvalidDateRangeException Handler to RentalRestControllerAdvice

> **Applied Skill:** `springboot.instructions.md` — module-scoped @RestControllerAdvice;
> `java.instructions.md` — ProblemDetail error response pattern with correlationId and errorCode

## 1. Objective

Add a new `@ExceptionHandler` method to `RentalRestControllerAdvice` that catches
`InvalidDateRangeException` and returns `HTTP 400 Bad Request` with a `ProblemDetail` body.
The body must carry `correlationId` (from MDC) and `errorCode = ErrorCodes.CONSTRAINT_VIOLATION`,
matching the contract defined in FR-02.

## 2. File to Modify / Create

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/rental/web/error/RentalRestControllerAdvice.java`
* **Action:** Modify Existing File

## 3. Code Implementation

**Imports Required:**

Add:

```java
import com.github.jenkaby.bikerental.rental.domain.exception.InvalidDateRangeException;
import com.github.jenkaby.bikerental.shared.web.advice.ErrorCodes;
```

> `HttpStatus`, `ProblemDetail`, `ResponseEntity`, `ExceptionHandler`, `ProblemDetailField`,
> and the `resolveCorrelationId()` method are already present in the class.

**Code to Add/Replace:**

* **Location:** Add the new handler method at the end of the class body, before the closing `}`.

```java
    @ExceptionHandler(InvalidDateRangeException.class)
    public ResponseEntity<ProblemDetail> handleInvalidDateRange(InvalidDateRangeException ex) {
        var correlationId = resolveCorrelationId();
        log.warn("[correlationId={}] Invalid date range: {}", correlationId, ex.getMessage());
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problem.setProperty(ProblemDetailField.CORRELATION_ID, correlationId);
        problem.setProperty(ProblemDetailField.ERROR_CODE, ErrorCodes.CONSTRAINT_VIOLATION);
        return ResponseEntity.of(problem).build();
    }
```

## 4. Validation Steps

skip
