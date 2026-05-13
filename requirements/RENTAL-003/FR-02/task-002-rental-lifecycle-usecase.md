# Task 002: Create RentalLifecycleUseCase Interface and RentalLifecycleService

> **Applied Skill:** `java.instructions.md` — Java Records for commands, pattern matching switch;
> `springboot.instructions.md` — constructor injection, stateless `@Service`

## 1. Objective

Create the orchestrator use case that routes an incoming lifecycle request to the appropriate
downstream use case (`ActivateRentalUseCase` or `CancelRentalUseCase`). The controller will call
only this interface; all routing logic lives here.

## 2. File to Modify / Create

### File A — Use Case Interface

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/rental/application/usecase/RentalLifecycleUseCase.java`
* **Action:** Create New File

```java
package com.github.jenkaby.bikerental.rental.application.usecase;

import com.github.jenkaby.bikerental.rental.domain.model.Rental;
import com.github.jenkaby.bikerental.rental.domain.model.RentalStatus;

public interface RentalLifecycleUseCase {

    Rental execute(RentalLifecycleCommand command);

    record RentalLifecycleCommand(
            Long rentalId,
            RentalStatus targetStatus,
            String operatorId
    ) {
    }
}
```

### File B — Service Implementation

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/rental/application/service/RentalLifecycleService.java`
* **Action:** Create New File

**Imports Required:**

```java
import com.github.jenkaby.bikerental.rental.application.usecase.ActivateRentalUseCase;
import com.github.jenkaby.bikerental.rental.application.usecase.CancelRentalUseCase;
import com.github.jenkaby.bikerental.rental.application.usecase.RentalLifecycleUseCase;
import com.github.jenkaby.bikerental.rental.domain.exception.InvalidRentalStatusException;
import com.github.jenkaby.bikerental.rental.domain.model.Rental;
import com.github.jenkaby.bikerental.rental.domain.model.RentalStatus;
import org.springframework.stereotype.Service;
```

```java
package com.github.jenkaby.bikerental.rental.application.service;

import com.github.jenkaby.bikerental.rental.application.usecase.ActivateRentalUseCase;
import com.github.jenkaby.bikerental.rental.application.usecase.CancelRentalUseCase;
import com.github.jenkaby.bikerental.rental.application.usecase.RentalLifecycleUseCase;
import com.github.jenkaby.bikerental.rental.domain.model.Rental;
import com.github.jenkaby.bikerental.rental.domain.model.RentalStatus;
import org.springframework.stereotype.Service;

@Service
class RentalLifecycleService implements RentalLifecycleUseCase {

    private final ActivateRentalUseCase activateRentalUseCase;
    private final CancelRentalUseCase cancelRentalUseCase;

    RentalLifecycleService(ActivateRentalUseCase activateRentalUseCase,
                           CancelRentalUseCase cancelRentalUseCase) {
        this.activateRentalUseCase = activateRentalUseCase;
        this.cancelRentalUseCase = cancelRentalUseCase;
    }

    @Override
    public Rental execute(RentalLifecycleCommand command) {
        return switch (command.targetStatus()) {
            case ACTIVE -> activateRentalUseCase.execute(
                    new ActivateRentalUseCase.ActivateCommand(command.rentalId(), command.operatorId()));
            case CANCELLED -> cancelRentalUseCase.execute(
                    new CancelRentalUseCase.CancelCommand(command.rentalId()));
            default -> throw new IllegalArgumentException(
                    "Unsupported lifecycle target status: " + command.targetStatus());
        };
    }
}
```

> **Note:** `ActivateRentalUseCase` and `CancelRentalUseCase` are created in FR-03 and FR-04.
> This file will not compile until those tasks are complete. Run compilation after FR-03 task-001
> and FR-04 task-004 are done.

## 4. Validation Steps

Run after FR-03 task-001 and FR-04 task-004 are also completed:

```bash
./gradlew :service:compileJava
```
