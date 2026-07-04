<task_file_template>

# Task 006: Map `AgreementPdfRenderingException` to HTTP 500 in the module advice

> **Applied Skill:** `.claude/rules/error-responses.md` — handlers return `ProblemDetail` with the mandatory
> `correlationId` and `errorCode` properties. Mirrors the existing handlers already present in
> `AgreementRestControllerAdvice`.

## 1. Objective

Add an `@ExceptionHandler` for `AgreementPdfRenderingException` that returns a 500 `ProblemDetail` carrying the standard
`correlationId` / `errorCode` shape. Depends on Task 005 (the exception).

## 2. File to Modify / Create

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/agreement/web/error/AgreementRestControllerAdvice.java`
* **Action:** Modify Existing File

## 3. Code Implementation

**Imports Required:**

Add this import in the import block, immediately AFTER the existing
`import com.github.jenkaby.bikerental.agreement.domain.exception.AgreementTemplateNotEditableException;` line:

```java
import com.github.jenkaby.bikerental.agreement.domain.exception.AgreementPdfRenderingException;
```

**Code to Add/Replace (handler method):**

* **Location:** Inside the class body, immediately AFTER the closing brace `}` of the existing
  `handleConcurrentActivation(DataIntegrityViolationException ex)` method and BEFORE the private `conflict(...)` helper
  method.
* **Snippet:**

```java
    @ExceptionHandler(AgreementPdfRenderingException.class)
    public ResponseEntity<ProblemDetail> handlePdfRendering(AgreementPdfRenderingException ex) {
        var correlationId = correlationIdProvider.resolve();
        log.error("[correlationId={}] Agreement PDF rendering failed: {}", correlationId, ex.getMessage(), ex);
        var problem = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        problem.setTitle("Agreement PDF rendering failed");
        problem.setDetail(ex.getMessage());
        problem.setProperty(ProblemDetailField.CORRELATION_ID, correlationId);
        problem.setProperty(ProblemDetailField.ERROR_CODE, ex.getErrorCode());
        return ResponseEntity.of(problem).build();
    }
```

> `HttpStatus`, `ProblemDetail`, `ResponseEntity`, `ExceptionHandler`, and `ProblemDetailField` are already imported in
> this file — do NOT re-import them. Only the `AgreementPdfRenderingException` import is new.

## 4. Validation Steps

Execute the following command. Do NOT run the full application server.

```bash
./gradlew :service:compileJava "-Dspring.profiles.active=test"
```

</task_file_template>
