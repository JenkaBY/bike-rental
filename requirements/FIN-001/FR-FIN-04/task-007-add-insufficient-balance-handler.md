# Task 007: Add `InsufficientBalanceException` Handler to `FinanceRestControllerAdvice`

> **Applied Skill:** N/A — follows the module-scoped `@RestControllerAdvice` handler pattern documented in
> `AGENTS.md` (`@Order(Ordered.LOWEST_PRECEDENCE - 1)`, module `basePackages` scope).

## 1. Objective

Register an `@ExceptionHandler` for `InsufficientBalanceException` in the finance module's scoped advice class.
Because `FinanceRestControllerAdvice` has `@Order(Ordered.LOWEST_PRECEDENCE - 1)`, it fires before
`CoreExceptionHandlerAdvice` and maps the balance failure to `422 Unprocessable Content` with the
`finance.insufficient_balance` error code and a `correlationId` for tracing.

## 2. File to Modify / Create

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/finance/web/error/FinanceRestControllerAdvice.java`
* **Action:** Modify Existing File

## 3. Code Implementation

**Imports Required:**

```java
import com.github.jenkaby.bikerental.finance.domain.exception.InsufficientBalanceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;

import static com.github.jenkaby.bikerental.shared.web.advice.ProblemDetailField.CORRELATION_ID;
import static com.github.jenkaby.bikerental.shared.web.advice.ProblemDetailField.ERROR_CODE;
```

**Code to Add/Replace:**

* **Location:** Inside `FinanceRestControllerAdvice`, add the new `@ExceptionHandler` method after the existing
  `resolveCorrelationId()` helper method.

Replace:

```java
    private String resolveCorrelationId() {
        String correlationId = MDC.get("correlationId");
        return correlationId != null ? correlationId : UUID.randomUUID().toString();
    }
}
```

With:

```java
    private String resolveCorrelationId() {
        String correlationId = MDC.get("correlationId");
        return correlationId != null ? correlationId : UUID.randomUUID().toString();
    }

    @ExceptionHandler(InsufficientBalanceException.class)
    ResponseEntity<ProblemDetail> handleInsufficientBalance(InsufficientBalanceException ex) {
        var correlationId = resolveCorrelationId();
        log.warn("[correlationId={}] Insufficient balance: {}", correlationId, ex.getMessage());
        var body = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_CONTENT, ex.getMessage());
        body.setProperty(CORRELATION_ID, correlationId);
        body.setProperty(ERROR_CODE, ex.getErrorCode());
        return new ResponseEntity<>(body, HttpStatus.UNPROCESSABLE_CONTENT);
    }
}
```

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```
