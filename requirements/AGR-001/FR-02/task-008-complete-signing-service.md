<task_file_template>

# Task 008: Create CompleteSigningService

> **Applied Skill:** `spring-boot-best-practices` — `@Service`, constructor injection, `@Transactional`;
> `java-best-practices` — zero inline comments. Mirrors `ActivateRentalService` event publishing (same
> `RentalEventMapper.toRentalStarted` + `EventPublisher` + `rental-events` exchange), but holds NO funds
> (already held at prepare) and enforces the fencing version.

## 1. Objective

Implement the version-fenced `AWAITING_SIGNATURE → ACTIVE` completion used only by the signing facade: verify
`expectedVersion` against the aggregate, convert `signedAt` (`Instant`) to `LocalDateTime` via the injected
`Clock` zone, call `completeSigning(startedAt)`, save, and publish `RentalStarted`.

## 2. File to Modify / Create

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/rental/application/service/CompleteSigningService.java`
* **Action:** Create New File

## 3. Code Implementation

Create the file with EXACTLY this content:

```java
package com.github.jenkaby.bikerental.rental.application.service;

import com.github.jenkaby.bikerental.rental.RentalSigningVersionMismatchException;
import com.github.jenkaby.bikerental.rental.application.mapper.RentalEventMapper;
import com.github.jenkaby.bikerental.rental.domain.model.Rental;
import com.github.jenkaby.bikerental.rental.domain.repository.RentalRepository;
import com.github.jenkaby.bikerental.shared.exception.ResourceNotFoundException;
import com.github.jenkaby.bikerental.shared.infrastructure.messaging.EventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;

@Slf4j
@Service
public class CompleteSigningService {

    private static final String RENTAL_EVENTS_EXCHANGER = "rental-events";

    private final RentalRepository rentalRepository;
    private final EventPublisher eventPublisher;
    private final RentalEventMapper eventMapper;
    private final Clock clock;

    CompleteSigningService(
            RentalRepository rentalRepository,
            EventPublisher eventPublisher,
            RentalEventMapper eventMapper,
            Clock clock) {
        this.rentalRepository = rentalRepository;
        this.eventPublisher = eventPublisher;
        this.eventMapper = eventMapper;
        this.clock = clock;
    }

    @Transactional
    public void completeSigning(Long rentalId, Long expectedVersion, Instant signedAt) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new ResourceNotFoundException(Rental.class, rentalId.toString()));

        if (!rental.getVersion().equals(expectedVersion)) {
            throw new RentalSigningVersionMismatchException(rentalId, expectedVersion, rental.getVersion());
        }

        LocalDateTime startedAt = LocalDateTime.ofInstant(signedAt, clock.getZone());
        rental.completeSigning(startedAt);

        Rental saved = rentalRepository.save(rental);
        eventPublisher.publish(RENTAL_EVENTS_EXCHANGER, eventMapper.toRentalStarted(saved));
        log.info("Rental {} signing completed at {}", saved.getId(), startedAt);
    }
}
```

> The class and its `completeSigning(...)` method are `public` because `RentalSigningFacadeImpl` (task 009)
> lives in the root package `...rental`, a DIFFERENT package from `...rental.application.service`, and must be
> able to call this method. The `public` type is still module-internal in Modulith terms (it is not in the
> root API package), so it does NOT widen the cross-module surface. Keep the constructor package-private as
> written.

## 4. Validation Steps

Execute the following command to ensure this task was successful. Do NOT run the full application server.

```bash
./gradlew :service:compileJava "-Dspring.profiles.active=test"
```

</task_file_template>
