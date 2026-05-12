# Task 004: Create CancelRentalUseCase Interface and CancelRentalService

> **Applied Skill:** `springboot.instructions.md` — constructor injection, `@Transactional`,
> stateless `@Service`; `java.instructions.md` — Java Records for commands

## 1. Objective

Create `CancelRentalUseCase` (interface + nested `CancelCommand`) and `CancelRentalService`
which: validates the transition, calls `FinanceFacade.releaseHold` when source status is ACTIVE,
calls `rental.cancel()` to update the aggregate, persists, and publishes `RentalCancelled`.

## 2. File to Modify / Create

### File A — Use Case Interface

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/rental/application/usecase/CancelRentalUseCase.java`
* **Action:** Create New File

```java
package com.github.jenkaby.bikerental.rental.application.usecase;

import com.github.jenkaby.bikerental.rental.domain.model.Rental;

public interface CancelRentalUseCase {

    Rental execute(CancelCommand command);

    record CancelCommand(Long rentalId) {
    }
}
```

### File B — Service Implementation

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/rental/application/service/CancelRentalService.java`
* **Action:** Create New File

**Imports Required:**

```java
import com.github.jenkaby.bikerental.finance.FinanceFacade;
import com.github.jenkaby.bikerental.rental.application.mapper.RentalEventMapper;
import com.github.jenkaby.bikerental.rental.application.usecase.CancelRentalUseCase;
import com.github.jenkaby.bikerental.rental.domain.model.Rental;
import com.github.jenkaby.bikerental.rental.domain.model.RentalStatus;
import com.github.jenkaby.bikerental.rental.domain.repository.RentalRepository;
import com.github.jenkaby.bikerental.shared.exception.ResourceNotFoundException;
import com.github.jenkaby.bikerental.shared.infrastructure.messaging.EventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
```

```java
package com.github.jenkaby.bikerental.rental.application.service;

import com.github.jenkaby.bikerental.finance.FinanceFacade;
import com.github.jenkaby.bikerental.rental.application.mapper.RentalEventMapper;
import com.github.jenkaby.bikerental.rental.application.usecase.CancelRentalUseCase;
import com.github.jenkaby.bikerental.rental.domain.model.Rental;
import com.github.jenkaby.bikerental.rental.domain.model.RentalStatus;
import com.github.jenkaby.bikerental.rental.domain.repository.RentalRepository;
import com.github.jenkaby.bikerental.shared.exception.ResourceNotFoundException;
import com.github.jenkaby.bikerental.shared.infrastructure.messaging.EventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
class CancelRentalService implements CancelRentalUseCase {

    private static final String RENTAL_EVENTS_EXCHANGER = "rental-events";

    private final RentalRepository rentalRepository;
    private final FinanceFacade financeFacade;
    private final EventPublisher eventPublisher;
    private final RentalEventMapper eventMapper;

    CancelRentalService(
            RentalRepository rentalRepository,
            FinanceFacade financeFacade,
            EventPublisher eventPublisher,
            RentalEventMapper eventMapper) {
        this.rentalRepository = rentalRepository;
        this.financeFacade = financeFacade;
        this.eventPublisher = eventPublisher;
        this.eventMapper = eventMapper;
    }

    @Override
    @Transactional
    public Rental execute(CancelCommand command) {
        Rental rental = rentalRepository.findById(command.rentalId())
                .orElseThrow(() -> new ResourceNotFoundException(Rental.class, command.rentalId().toString()));

        rental.getStatus().validateTransitionTo(RentalStatus.CANCELLED);

        if (rental.getStatus() == RentalStatus.ACTIVE) {
            log.info("Releasing hold for ACTIVE rental {}", rental.getId());
            financeFacade.releaseHold(rental.toRentalRef());
        }

        rental.cancel();

        Rental saved = rentalRepository.save(rental);
        eventPublisher.publish(RENTAL_EVENTS_EXCHANGER, eventMapper.toRentalCancelled(saved));
        log.info("Rental {} cancelled", saved.getId());
        return saved;
    }
}
```

## 4. Validation Steps

skip