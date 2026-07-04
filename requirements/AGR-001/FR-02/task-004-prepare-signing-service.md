<task_file_template>

# Task 004: Create PrepareSigningService

> **Applied Skill:** `spring-boot-best-practices` — `@Service`, constructor injection, `@Transactional`;
> `java-best-practices` — zero inline comments, `final` fields. Mirrors `ActivateRentalService` (load → domain
> method → conditional hold → save), but publishes NO event (rental has not started).

## 1. Objective

Implement the `DRAFT → AWAITING_SIGNATURE` transition: load the rental (404 if absent), call
`prepareForSigning()`, hold funds when `estimatedCost` is positive, save, and return. No event is published.

## 2. File to Modify / Create

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/rental/application/service/PrepareSigningService.java`
* **Action:** Create New File

## 3. Code Implementation

Create the file with EXACTLY this content:

```java
package com.github.jenkaby.bikerental.rental.application.service;

import com.github.jenkaby.bikerental.finance.FinanceFacade;
import com.github.jenkaby.bikerental.rental.application.usecase.PrepareSigningUseCase;
import com.github.jenkaby.bikerental.rental.domain.model.Rental;
import com.github.jenkaby.bikerental.rental.domain.repository.RentalRepository;
import com.github.jenkaby.bikerental.shared.domain.CustomerRef;
import com.github.jenkaby.bikerental.shared.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
class PrepareSigningService implements PrepareSigningUseCase {

    private final RentalRepository rentalRepository;
    private final FinanceFacade financeFacade;

    PrepareSigningService(RentalRepository rentalRepository, FinanceFacade financeFacade) {
        this.rentalRepository = rentalRepository;
        this.financeFacade = financeFacade;
    }

    @Override
    @Transactional
    public Rental execute(PrepareSigningCommand command) {
        Rental rental = rentalRepository.findById(command.rentalId())
                .orElseThrow(() -> new ResourceNotFoundException(Rental.class, command.rentalId().toString()));

        rental.prepareForSigning();

        if (rental.getEstimatedCost().isPositive()) {
            var holdInfo = financeFacade.holdFunds(
                    new CustomerRef(rental.getCustomerId()),
                    rental.toRentalRef(),
                    rental.getEstimatedCost(),
                    command.operatorId());
            log.info("Funds held for signing of rental {}: transactionId={}, heldAt={}",
                    rental.getId(), holdInfo.transactionRef().id(), holdInfo.recordedAt());
        }

        Rental saved = rentalRepository.save(rental);
        log.info("Rental {} moved to AWAITING_SIGNATURE", saved.getId());
        return saved;
    }
}
```

> `prepareForSigning()` runs BEFORE the hold, so an unready draft or invalid transition aborts with 422 before
> any finance side effect — this satisfies FR scenario 4 ("no hold is created"). Do NOT set `startedAt`.

## 4. Validation Steps

Execute the following command to ensure this task was successful. Do NOT run the full application server.

```bash
./gradlew :service:compileJava "-Dspring.profiles.active=test"
```

</task_file_template>
