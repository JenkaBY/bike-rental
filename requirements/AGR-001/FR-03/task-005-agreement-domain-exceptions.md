<task_file_template>

# Task 005: Create the four agreement domain exceptions

> **Applied Skill:** `error-responses` rule — each exception exposes a stable `getErrorCode()` string
> surfaced by a module-scoped advice; codes are documented in `docs/error-codes.md` (Task 019).
> `java-best-practices` — zero inline comments; records for immutable detail carriers. Mirrors
> `rental/domain/exception/InvalidRentalStatusException.java` (extends shared `BikeRentalException`,
> `public static final String ERROR_CODE`, nested `Details` record via `getParams()`).

## 1. Objective

Add the four domain exceptions the aggregate throws to guard its lifecycle, each carrying its own
error code and a `currentStatus` detail so the advice (Task 015) can build a `ProblemDetail`.

## 2. Files to Modify / Create

Create FOUR new files under
`service/src/main/java/com/github/jenkaby/bikerental/agreement/domain/exception/`.

## 3. Code Implementation

### File 1: `AgreementTemplateNotEditableException.java`

Create with EXACTLY this content:

```java
package com.github.jenkaby.bikerental.agreement.domain.exception;

import com.github.jenkaby.bikerental.agreement.domain.model.AgreementTemplateStatus;
import com.github.jenkaby.bikerental.shared.exception.BikeRentalException;
import lombok.Getter;

@Getter
public class AgreementTemplateNotEditableException extends BikeRentalException {

    public static final String ERROR_CODE = "agreement.template.not_editable";

    private static final String MESSAGE_TEMPLATE = "Cannot edit agreement template in status %s. Only DRAFT templates are editable";

    public AgreementTemplateNotEditableException(AgreementTemplateStatus currentStatus) {
        super(MESSAGE_TEMPLATE.formatted(currentStatus), ERROR_CODE, new Details(currentStatus));
    }

    public Details getDetails() {
        return getParams()
                .map(d -> (Details) d)
                .orElseThrow(() -> new IllegalArgumentException("Expected Details in exception parameters"));
    }

    public record Details(AgreementTemplateStatus currentStatus) {
    }
}
```

### File 2: `AgreementTemplateNotActivatableException.java`

Create with EXACTLY this content:

```java
package com.github.jenkaby.bikerental.agreement.domain.exception;

import com.github.jenkaby.bikerental.agreement.domain.model.AgreementTemplateStatus;
import com.github.jenkaby.bikerental.shared.exception.BikeRentalException;
import lombok.Getter;

@Getter
public class AgreementTemplateNotActivatableException extends BikeRentalException {

    public static final String ERROR_CODE = "agreement.template.not_activatable";

    private static final String MESSAGE_TEMPLATE = "Cannot activate agreement template in status %s. Only DRAFT templates are activatable";

    public AgreementTemplateNotActivatableException(AgreementTemplateStatus currentStatus) {
        super(MESSAGE_TEMPLATE.formatted(currentStatus), ERROR_CODE, new Details(currentStatus));
    }

    public Details getDetails() {
        return getParams()
                .map(d -> (Details) d)
                .orElseThrow(() -> new IllegalArgumentException("Expected Details in exception parameters"));
    }

    public record Details(AgreementTemplateStatus currentStatus) {
    }
}
```

### File 3: `AgreementTemplateNotDeletableException.java`

Create with EXACTLY this content:

```java
package com.github.jenkaby.bikerental.agreement.domain.exception;

import com.github.jenkaby.bikerental.agreement.domain.model.AgreementTemplateStatus;
import com.github.jenkaby.bikerental.shared.exception.BikeRentalException;
import lombok.Getter;

@Getter
public class AgreementTemplateNotDeletableException extends BikeRentalException {

    public static final String ERROR_CODE = "agreement.template.not_deletable";

    private static final String MESSAGE_TEMPLATE = "Cannot delete agreement template in status %s. Only DRAFT templates are deletable";

    public AgreementTemplateNotDeletableException(AgreementTemplateStatus currentStatus) {
        super(MESSAGE_TEMPLATE.formatted(currentStatus), ERROR_CODE, new Details(currentStatus));
    }

    public Details getDetails() {
        return getParams()
                .map(d -> (Details) d)
                .orElseThrow(() -> new IllegalArgumentException("Expected Details in exception parameters"));
    }

    public record Details(AgreementTemplateStatus currentStatus) {
    }
}
```

### File 4: `ActiveAgreementTemplateNotFoundException.java`

Create with EXACTLY this content:

```java
package com.github.jenkaby.bikerental.agreement.domain.exception;

import com.github.jenkaby.bikerental.shared.exception.BikeRentalException;

public class ActiveAgreementTemplateNotFoundException extends BikeRentalException {

    public static final String ERROR_CODE = "agreement.template.no_active";

    private static final String MESSAGE = "No active agreement template exists";

    public ActiveAgreementTemplateNotFoundException() {
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
