<task_file_template>

# Task 006: Create the four signing domain exceptions

> **Applied Skill:** `error-responses` / `java-best-practices` — each exception extends `shared.exception.BikeRentalException`,
> exposes a `public static final String ERROR_CODE`, and carries a `Details` record for structured `params` (mirrors
> `AgreementTemplateNotEditableException`). Zero inline comments. `InvalidSignatureImageException` carries no params.

## 1. Objective

Create the four new signing domain exceptions with their agreed error codes. They are handled by the advice (Task 018)
and catalogued in docs (Task 019).

## 2. File to Modify / Create

Create FOUR new files, each with EXACTLY the content below.

## 3. Code Implementation

**File 1 — `service/src/main/java/com/github/jenkaby/bikerental/agreement/domain/exception/AgreementAlreadySignedException.java`** (Create New File):

```java
package com.github.jenkaby.bikerental.agreement.domain.exception;

import com.github.jenkaby.bikerental.shared.exception.BikeRentalException;

public class AgreementAlreadySignedException extends BikeRentalException {

    public static final String ERROR_CODE = "agreement.signing.already_signed";

    private static final String MESSAGE_TEMPLATE = "Rental %d has already been signed";

    public AgreementAlreadySignedException(Long rentalId) {
        super(MESSAGE_TEMPLATE.formatted(rentalId), ERROR_CODE, new Details(rentalId));
    }

    public Details getDetails() {
        return getParams()
                .map(d -> (Details) d)
                .orElseThrow(() -> new IllegalArgumentException("Expected Details in exception parameters"));
    }

    public record Details(Long rentalId) {
    }
}
```

**File 2 — `service/src/main/java/com/github/jenkaby/bikerental/agreement/domain/exception/AgreementTemplateNotActiveException.java`** (Create New File):

```java
package com.github.jenkaby.bikerental.agreement.domain.exception;

import com.github.jenkaby.bikerental.shared.exception.BikeRentalException;

public class AgreementTemplateNotActiveException extends BikeRentalException {

    public static final String ERROR_CODE = "agreement.template.not_active";

    private static final String MESSAGE_TEMPLATE = "Template %d is not the current active template. Active template is %d";

    public AgreementTemplateNotActiveException(Long requestedTemplateId, Long activeTemplateId) {
        super(MESSAGE_TEMPLATE.formatted(requestedTemplateId, activeTemplateId), ERROR_CODE,
                new Details(requestedTemplateId, activeTemplateId));
    }

    public Details getDetails() {
        return getParams()
                .map(d -> (Details) d)
                .orElseThrow(() -> new IllegalArgumentException("Expected Details in exception parameters"));
    }

    public record Details(Long requestedTemplateId, Long activeTemplateId) {
    }
}
```

**File 3 — `service/src/main/java/com/github/jenkaby/bikerental/agreement/domain/exception/SigningVersionMismatchException.java`** (Create New File):

```java
package com.github.jenkaby.bikerental.agreement.domain.exception;

import com.github.jenkaby.bikerental.shared.exception.BikeRentalException;

public class SigningVersionMismatchException extends BikeRentalException {

    public static final String ERROR_CODE = "agreement.signing.rental_version_mismatch";

    private static final String MESSAGE_TEMPLATE = "Rental %d version mismatch. Expected: %d, actual: %d";

    public SigningVersionMismatchException(Long rentalId, Long expectedVersion, Long actualVersion) {
        super(MESSAGE_TEMPLATE.formatted(rentalId, expectedVersion, actualVersion), ERROR_CODE,
                new Details(rentalId, expectedVersion, actualVersion));
    }

    public Details getDetails() {
        return getParams()
                .map(d -> (Details) d)
                .orElseThrow(() -> new IllegalArgumentException("Expected Details in exception parameters"));
    }

    public record Details(Long rentalId, Long expectedVersion, Long actualVersion) {
    }
}
```

**File 4 — `service/src/main/java/com/github/jenkaby/bikerental/agreement/domain/exception/InvalidSignatureImageException.java`** (Create New File):

```java
package com.github.jenkaby.bikerental.agreement.domain.exception;

import com.github.jenkaby.bikerental.shared.exception.BikeRentalException;

public class InvalidSignatureImageException extends BikeRentalException {

    public static final String ERROR_CODE = "agreement.signing.invalid_signature_image";

    private static final String MESSAGE = "The submitted signature image is not valid base64-encoded PNG data";

    public InvalidSignatureImageException() {
        super(MESSAGE, ERROR_CODE);
    }
}
```

## 4. Validation Steps

Execute the following command. Do NOT run the full application server.

```bash
./gradlew :service:compileJava "-Dspring.profiles.active=test"
```

</task_file_template>
