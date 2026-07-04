<task_file_template>

# Task 009: Create RentalSigningFacade and package-private RentalSigningFacadeImpl

> **Applied Skill:** `spring-boot-modulith` — public interface + package-private `@Service` impl in the module
> root package (the `FinanceFacade` / `FinanceFacadeImpl` pattern); `java-best-practices` — records, streams,
> `Stream.toList()`, zero inline comments.

## 1. Objective

Expose the rental signing API to the agreement module: `getSigningSnapshot(rentalId)` (only when status is
`AWAITING_SIGNATURE`) and `completeSigning(rentalId, expectedVersion, signedAt)` (delegates to
`CompleteSigningService`). The impl reads the aggregate via `RentalRepository` and maps it to
`RentalSigningSnapshot`.

## 2. File to Modify / Create

* **File Path 1:** `service/src/main/java/com/github/jenkaby/bikerental/rental/RentalSigningFacade.java`
* **File Path 2:** `service/src/main/java/com/github/jenkaby/bikerental/rental/RentalSigningFacadeImpl.java`
* **Action:** Create New File (both)

## 3. Code Implementation

### File 1 — `RentalSigningFacade.java`

Create the file with EXACTLY this content:

```java
package com.github.jenkaby.bikerental.rental;

import java.time.Instant;

public interface RentalSigningFacade {

    RentalSigningSnapshot getSigningSnapshot(Long rentalId);

    void completeSigning(Long rentalId, Long expectedVersion, Instant signedAt);
}
```

### File 2 — `RentalSigningFacadeImpl.java`

Create the file with EXACTLY this content:

```java
package com.github.jenkaby.bikerental.rental;

import com.github.jenkaby.bikerental.rental.application.service.CompleteSigningService;
import com.github.jenkaby.bikerental.rental.domain.model.Rental;
import com.github.jenkaby.bikerental.rental.domain.model.RentalEquipment;
import com.github.jenkaby.bikerental.rental.domain.model.RentalStatus;
import com.github.jenkaby.bikerental.rental.domain.repository.RentalRepository;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
import com.github.jenkaby.bikerental.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
class RentalSigningFacadeImpl implements RentalSigningFacade {

    private final RentalRepository rentalRepository;
    private final CompleteSigningService completeSigningService;

    RentalSigningFacadeImpl(RentalRepository rentalRepository, CompleteSigningService completeSigningService) {
        this.rentalRepository = rentalRepository;
        this.completeSigningService = completeSigningService;
    }

    @Override
    public RentalSigningSnapshot getSigningSnapshot(Long rentalId) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new ResourceNotFoundException(Rental.class, rentalId.toString()));

        if (rental.getStatus() != RentalStatus.AWAITING_SIGNATURE) {
            throw new RentalNotAwaitingSignatureException(rentalId, rental.getStatus());
        }

        List<RentalSigningSnapshot.EquipmentItem> equipments = rental.getEquipments().stream()
                .map(this::toEquipmentItem)
                .toList();

        return new RentalSigningSnapshot(
                rental.getId(),
                rental.getVersion(),
                rental.getCustomerId(),
                rental.getPlannedDuration(),
                amountOf(rental.getEstimatedCost()),
                equipments);
    }

    @Override
    public void completeSigning(Long rentalId, Long expectedVersion, Instant signedAt) {
        completeSigningService.completeSigning(rentalId, expectedVersion, signedAt);
    }

    private RentalSigningSnapshot.EquipmentItem toEquipmentItem(RentalEquipment equipment) {
        return new RentalSigningSnapshot.EquipmentItem(
                equipment.getEquipmentId(),
                equipment.getEquipmentUid(),
                equipment.getEquipmentType(),
                amountOf(equipment.getEstimatedCost()));
    }

    private BigDecimal amountOf(Money money) {
        return money == null ? null : money.amount();
    }
}
```

> `getEstimatedCost()` on the aggregate returns a `Money` (never null when the rental is ready); per-equipment
> `getEstimatedCost()` may be null, hence the null-safe `amountOf`. `equipmentType` on `RentalEquipment` holds
> the type slug. Do NOT annotate the impl `public` — it must remain package-private so only the interface is
> the cross-module surface.

## 4. Validation Steps

Execute the following command to ensure this task was successful. Do NOT run the full application server.

```bash
./gradlew :service:compileJava "-Dspring.profiles.active=test"
```

</task_file_template>
