<task_file_template>

# Task 005: Create the `AgreementPdfRenderingException` domain exception

> **Applied Skill:** `.claude/rules/error-responses.md` — every domain error carries a stable `errorCode`; the code is
> catalogued in `docs/error-codes.md` (Task 012). Mirrors the existing agreement exceptions (e.g.
> `AgreementTemplateNotActivatableException`) extending `BikeRentalException`.

## 1. Objective

Provide a domain exception that wraps any low-level `IOException` raised while producing the PDF, carrying the stable
code `agreement.pdf.rendering_failed`. It is mapped to HTTP 500 by the module advice (Task 006). Not expected in normal
operation.

## 2. File to Modify / Create

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/agreement/domain/exception/AgreementPdfRenderingException.java`
* **Action:** Create New File

## 3. Code Implementation

Create the file with EXACTLY this content:

```java
package com.github.jenkaby.bikerental.agreement.domain.exception;

import com.github.jenkaby.bikerental.shared.exception.BikeRentalException;
import lombok.Getter;

@Getter
public class AgreementPdfRenderingException extends BikeRentalException {

    public static final String ERROR_CODE = "agreement.pdf.rendering_failed";

    private static final String MESSAGE = "Failed to render the agreement PDF document";

    public AgreementPdfRenderingException(Throwable cause) {
        super(MESSAGE, ERROR_CODE);
        initCause(cause);
    }
}
```

> `BikeRentalException` has no cause-carrying constructor, so the original `IOException` is preserved via
> `initCause(cause)` after the `super(...)` call. Do NOT change `BikeRentalException`.

## 4. Validation Steps

Execute the following command. Do NOT run the full application server.

```bash
./gradlew :service:compileJava "-Dspring.profiles.active=test"
```

</task_file_template>
