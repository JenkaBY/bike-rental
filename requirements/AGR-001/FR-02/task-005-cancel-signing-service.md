<task_file_template>

# Task 005: Create CancelSigningService

> **Applied Skill:** `spring-boot-best-practices` — `@Service`, constructor injection, `@Transactional`;
> `java-best-practices` — zero inline comments, `final` fields. Mirrors `CancelRentalService` for the
> hold-release guard, but transitions `AWAITING_SIGNATURE → DRAFT` and publishes NO event.

## 1. Objective

Implement the `AWAITING_SIGNATURE → DRAFT` transition: load the rental (404 if absent), call `cancelSigning()`,
release the hold if one exists, save, and return. No event is published.

## 2. File to Modify / Create

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/rental/application/service/CancelSigningService.java`
* **Action:** Create New File

## 3. Code Implementation

Create the file with EXACTLY this content:

```java
package com.github.jenkaby.bikerental.rental.application.service;

import com.github.jenkaby.bikerental.finance.FinanceFacade;
import com.github.jenkaby.bikerental.rental.application.usecase.CancelSigningUseCase;
import com.github.jenkaby.bikerental.rental.domain.model.Rental;
import com.github.jenkaby.bikerental.rental.domain.repository.RentalRepository;
import com.github.jenkaby.bikerental.shared.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
class CancelSigningService implements CancelSigningUseCase {

    private final RentalRepository rentalRepository;
    private final FinanceFacade financeFacade;

    CancelSigningService(RentalRepository rentalRepository, FinanceFacade financeFacade) {
        this.rentalRepository = rentalRepository;
        this.financeFacade = financeFacade;
    }

    @Override
    @Transactional
    public Rental execute(CancelSigningCommand command) {
        Rental rental = rentalRepository.findById(command.rentalId())
                .orElseThrow(() -> new ResourceNotFoundException(Rental.class, command.rentalId().toString()));

        rental.cancelSigning();

        if (financeFacade.hasHold(rental.toRentalRef())) {
            log.info("Releasing hold for cancelled signing of rental {}", rental.getId());
            financeFacade.releaseHold(rental.toRentalRef(), command.operatorId());
        }

        Rental saved = rentalRepository.save(rental);
        log.info("Rental {} moved back to DRAFT (signing cancelled)", saved.getId());
        return saved;
    }
}
```

> `cancelSigning()` runs first so a wrong-status rental aborts with 422 before touching finance. The
> `hasHold` guard makes release a no-op for a zero-cost rental (FR scenario 5 path). Do NOT publish an event.

## 4. Validation Steps

Execute the following command to ensure this task was successful. Do NOT run the full application server.

```bash
./gradlew :service:compileJava "-Dspring.profiles.active=test"
```

</task_file_template>
