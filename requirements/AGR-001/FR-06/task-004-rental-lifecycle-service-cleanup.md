<task_file_template>

# Task 004: Remove the ACTIVE branch and ActivateRentalUseCase dependency from RentalLifecycleService

> **Applied Skill:** `spring-boot-best-practices` — constructor injection updated in lockstep with the removed
> dependency; `java-style.md` zero-inline-comments respected (no comment added to explain the removal). Depends on
> Task 003 (the `ActivateRentalUseCase` interface no longer exists).

## 1. Objective

Remove the `activateRentalUseCase` field, its constructor parameter/assignment, its import, and the `case ACTIVE`
switch branch from `RentalLifecycleService`. The command's `targetStatus` is `RentalStatus` (domain enum, which
still has an `ACTIVE` constant), so the `switch` remains valid as a non-exhaustive `switch` with the existing
`default` arm — do not add a new `default` branch, one already exists.

## 2. File to Modify / Create

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/rental/application/service/RentalLifecycleService.java`
* **Action:** Modify Existing File

## 3. Code Implementation

**Imports Required:**

Remove this import line entirely:

```java
import com.github.jenkaby.bikerental.rental.application.usecase.ActivateRentalUseCase;
```

**Code to Add/Replace:**

Replace the ENTIRE file content with EXACTLY this (removes the `activateRentalUseCase` field, constructor parameter,
assignment, import, and the `case ACTIVE` switch arm; everything else is unchanged):

```java
package com.github.jenkaby.bikerental.rental.application.service;

import com.github.jenkaby.bikerental.rental.application.usecase.CancelRentalUseCase;
import com.github.jenkaby.bikerental.rental.application.usecase.CancelSigningUseCase;
import com.github.jenkaby.bikerental.rental.application.usecase.PrepareSigningUseCase;
import com.github.jenkaby.bikerental.rental.application.usecase.RentalLifecycleUseCase;
import com.github.jenkaby.bikerental.rental.domain.model.Rental;
import org.springframework.stereotype.Service;

@Service
class RentalLifecycleService implements RentalLifecycleUseCase {

    private final CancelRentalUseCase cancelRentalUseCase;
    private final PrepareSigningUseCase prepareSigningUseCase;
    private final CancelSigningUseCase cancelSigningUseCase;

    RentalLifecycleService(CancelRentalUseCase cancelRentalUseCase,
                           PrepareSigningUseCase prepareSigningUseCase,
                           CancelSigningUseCase cancelSigningUseCase) {
        this.cancelRentalUseCase = cancelRentalUseCase;
        this.prepareSigningUseCase = prepareSigningUseCase;
        this.cancelSigningUseCase = cancelSigningUseCase;
    }

    @Override
    public Rental execute(RentalLifecycleCommand command) {
        return switch (command.targetStatus()) {
            case CANCELLED -> cancelRentalUseCase.execute(
                    new CancelRentalUseCase.CancelCommand(command.rentalId(), command.operatorId()));
            case AWAITING_SIGNATURE -> prepareSigningUseCase.execute(
                    new PrepareSigningUseCase.PrepareSigningCommand(command.rentalId(), command.operatorId()));
            case DRAFT -> cancelSigningUseCase.execute(
                    new CancelSigningUseCase.CancelSigningCommand(command.rentalId(), command.operatorId()));
            default -> throw new IllegalArgumentException(
                    "Unsupported lifecycle target status: " + command.targetStatus());
        };
    }
}
```

## 4. Validation Steps

Execute the following command. Do NOT run the full application server.

```bash
./gradlew :service:compileJava "-Dspring.profiles.active=test"
```

</task_file_template>
