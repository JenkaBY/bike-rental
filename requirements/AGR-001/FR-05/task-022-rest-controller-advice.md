<task_file_template>

# Task 022: Extend AgreementRestControllerAdvice with the signing handlers

> **Applied Skill:** `error-responses` — each handler returns a `ProblemDetail` with `correlationId` + `errorCode`; the
> new agreement exceptions expose their own `ERROR_CODE`; the two rental-module-API exceptions are mapped with agreement
> `signing.*` codes declared as local constants. 409 for conflict-style, 400 for the invalid image. NO handler for
> `ObjectOptimisticLockingFailureException` (global advice handles it). Reuses the private `conflict(...)` helper for the
> params-carrying agreement exceptions. Depends on Task 006.

## 1. Objective

Add exception handlers for the six new signing failure cases to the existing agreement advice.

## 2. File to Modify / Create

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/agreement/web/error/AgreementRestControllerAdvice.java`
* **Action:** Modify Existing File

## 3. Code Implementation

**Imports Required:** Add these import lines alongside the existing imports at the top of the file:

```java
import com.github.jenkaby.bikerental.agreement.domain.exception.AgreementAlreadySignedException;
import com.github.jenkaby.bikerental.agreement.domain.exception.AgreementTemplateNotActiveException;
import com.github.jenkaby.bikerental.agreement.domain.exception.InvalidSignatureImageException;
import com.github.jenkaby.bikerental.agreement.domain.exception.SigningVersionMismatchException;
import com.github.jenkaby.bikerental.rental.RentalNotAwaitingSignatureException;
import com.github.jenkaby.bikerental.rental.RentalSigningVersionMismatchException;
```

**Code to Add (constant):** Add this constant right below the existing `CONCURRENT_ACTIVATION_ERROR_CODE` constant:

```java
    private static final String RENTAL_NOT_AWAITING_SIGNATURE_ERROR_CODE = "agreement.signing.rental_not_awaiting_signature";
    private static final String RENTAL_VERSION_MISMATCH_ERROR_CODE = "agreement.signing.rental_version_mismatch";
    private static final String INVALID_SIGNATURE_IMAGE_ERROR_CODE = "agreement.signing.invalid_signature_image";
```

**Code to Add (handlers):** Add these handler methods right ABOVE the private `conflict(...)` method:

```java
    @ExceptionHandler(AgreementAlreadySignedException.class)
    public ResponseEntity<ProblemDetail> handleAlreadySigned(AgreementAlreadySignedException ex) {
        return conflict(ex.getMessage(), ex.getErrorCode(), ex.getDetails());
    }

    @ExceptionHandler(AgreementTemplateNotActiveException.class)
    public ResponseEntity<ProblemDetail> handleTemplateNotActive(AgreementTemplateNotActiveException ex) {
        return conflict(ex.getMessage(), ex.getErrorCode(), ex.getDetails());
    }

    @ExceptionHandler(SigningVersionMismatchException.class)
    public ResponseEntity<ProblemDetail> handleSigningVersionMismatch(SigningVersionMismatchException ex) {
        return conflict(ex.getMessage(), ex.getErrorCode(), ex.getDetails());
    }

    @ExceptionHandler(RentalNotAwaitingSignatureException.class)
    public ResponseEntity<ProblemDetail> handleRentalNotAwaitingSignature(RentalNotAwaitingSignatureException ex) {
        return conflict(ex.getMessage(), RENTAL_NOT_AWAITING_SIGNATURE_ERROR_CODE, null);
    }

    @ExceptionHandler(RentalSigningVersionMismatchException.class)
    public ResponseEntity<ProblemDetail> handleRentalSigningVersionMismatch(RentalSigningVersionMismatchException ex) {
        return conflict(ex.getMessage(), RENTAL_VERSION_MISMATCH_ERROR_CODE, null);
    }

    @ExceptionHandler(InvalidSignatureImageException.class)
    public ResponseEntity<ProblemDetail> handleInvalidSignatureImage(InvalidSignatureImageException ex) {
        var correlationId = correlationIdProvider.resolve();
        log.warn("[correlationId={}] Invalid signature image [{}]: {}", correlationId, ex.getErrorCode(), ex.getMessage());
        var problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setTitle("Invalid signature image");
        problem.setDetail(ex.getMessage());
        problem.setProperty(ProblemDetailField.CORRELATION_ID, correlationId);
        problem.setProperty(ProblemDetailField.ERROR_CODE, INVALID_SIGNATURE_IMAGE_ERROR_CODE);
        return ResponseEntity.of(problem).build();
    }
```

> The `conflict(...)` helper already sets status 409 and the `PARAMS` property (null-safe). Pass `null` params for the
> two rental-module exceptions (they are not `BikeRentalException` and carry no `Details`). Do NOT add a handler for
> `ObjectOptimisticLockingFailureException` here.

## 4. Validation Steps

Execute the following command. Do NOT run the full application server.

```bash
./gradlew :service:compileJava "-Dspring.profiles.active=test"
```

</task_file_template>
