# Task 001: Create ActivateRentalUseCase Interface and ActivateRentalService

> **Applied Skill:** `springboot.instructions.md` — constructor injection, `@Transactional`,
> stateless `@Service`; `java.instructions.md` — Java Records for commands

## 1. Objective

Create the `ActivateRentalUseCase` interface with its `ActivateCommand` record, and
`ActivateRentalService` which: validates the transition via the state machine, calls
`FinanceFacade.holdFunds` when cost > 0, activates the rental aggregate, persists it, and
publishes `RentalStarted`.

## 2. File to Modify / Create

### File A — Use Case Interface

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/rental/application/usecase/ActivateRentalUseCase.java`
* **Action:** Create New File

```java
package com.github.jenkaby.bikerental.rental.application.usecase;

import com.github.jenkaby.bikerental.rental.domain.model.Rental;

public interface ActivateRentalUseCase {

    Rental execute(ActivateCommand command);

    record ActivateCommand(Long rentalId, String operatorId) {
    }
}
```

### File B — Service Implementation

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/rental/application/service/ActivateRentalService.java`
* **Action:** Create New File

**Imports Required:**

```java
import com.github.jenkaby.bikerental.finance.FinanceFacade;
import com.github.jenkaby.bikerental.rental.application.mapper.RentalEventMapper;
import com.github.jenkaby.bikerental.rental.application.usecase.ActivateRentalUseCase;
import com.github.jenkaby.bikerental.rental.domain.model.Rental;
import com.github.jenkaby.bikerental.rental.domain.model.RentalStatus;
import com.github.jenkaby.bikerental.rental.domain.repository.RentalRepository;
import com.github.jenkaby.bikerental.shared.domain.CustomerRef;
import com.github.jenkaby.bikerental.shared.exception.ResourceNotFoundException;
import com.github.jenkaby.bikerental.shared.infrastructure.messaging.EventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
```

```java
package com.github.jenkaby.bikerental.rental.application.service;

import com.github.jenkaby.bikerental.finance.FinanceFacade;
import com.github.jenkaby.bikerental.rental.application.mapper.RentalEventMapper;
import com.github.jenkaby.bikerental.rental.application.usecase.ActivateRentalUseCase;
import com.github.jenkaby.bikerental.rental.domain.model.Rental;
import com.github.jenkaby.bikerental.rental.domain.model.RentalStatus;
import com.github.jenkaby.bikerental.rental.domain.repository.RentalRepository;
import com.github.jenkaby.bikerental.shared.domain.CustomerRef;
import com.github.jenkaby.bikerental.shared.exception.ResourceNotFoundException;
import com.github.jenkaby.bikerental.shared.infrastructure.messaging.EventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

@Slf4j
@Service
class ActivateRentalService implements ActivateRentalUseCase {

    private static final String RENTAL_EVENTS_EXCHANGER = "rental-events";

    private final RentalRepository rentalRepository;
    private final FinanceFacade financeFacade;
    private final EventPublisher eventPublisher;
    private final RentalEventMapper eventMapper;
    private final Clock clock;

    ActivateRentalService(
            RentalRepository rentalRepository,
            FinanceFacade financeFacade,
            EventPublisher eventPublisher,
            RentalEventMapper eventMapper,
            Clock clock) {
        this.rentalRepository = rentalRepository;
        this.financeFacade = financeFacade;
        this.eventPublisher = eventPublisher;
        this.eventMapper = eventMapper;
        this.clock = clock;
    }

    @Override
    @Transactional
    public Rental execute(ActivateCommand command) {
        Rental rental = rentalRepository.findById(command.rentalId())
                .orElseThrow(() -> new ResourceNotFoundException(Rental.class, command.rentalId().toString()));

        rental.getStatus().validateTransitionTo(RentalStatus.ACTIVE);

        if (rental.getEstimatedCost().isPositive()) {
            var holdInfo = financeFacade.holdFunds(
                    new CustomerRef(rental.getCustomerId()),
                    rental.toRentalRef(),
                    rental.getEstimatedCost(),
                    command.operatorId());
            log.info("Funds held for rental {}: transactionId={}, heldAt={}",
                    rental.getId(), holdInfo.transactionRef().id(), holdInfo.recordedAt());
        }

        LocalDateTime startedAt = LocalDateTime.now(clock);
        rental.activate(startedAt);

        Rental saved = rentalRepository.save(rental);
        eventPublisher.publish(RENTAL_EVENTS_EXCHANGER, eventMapper.toRentalStarted(saved));
        log.info("Rental {} activated at {}", saved.getId(), startedAt);
        return saved;
    }
}
```

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```
