<task_file_template>

# Task 007: Create Rental module-API DTOs and signing exceptions in the root package

> **Applied Skill:** `spring-boot-modulith` — cross-module surface lives in the module root package (the
> Modulith API layer), following the `FinanceFacade` pattern (interface + record DTOs + public exceptions +
> package-private impl); `java-best-practices` — records for DTOs, zero inline comments.

## 1. Objective

Create the public read model (`RentalSigningSnapshot` with nested `EquipmentItem`) and the two public
exceptions (`RentalNotAwaitingSignatureException`, `RentalSigningVersionMismatchException`) that the agreement
module (FR-05) will reference. All live in `com.github.jenkaby.bikerental.rental` (root package).

## 2. File to Modify / Create

* **File Path 1:** `service/src/main/java/com/github/jenkaby/bikerental/rental/RentalSigningSnapshot.java`
* **File Path 2:** `service/src/main/java/com/github/jenkaby/bikerental/rental/RentalNotAwaitingSignatureException.java`
* **File Path 3:** `service/src/main/java/com/github/jenkaby/bikerental/rental/RentalSigningVersionMismatchException.java`
* **Action:** Create New File (all three)

## 3. Code Implementation

### File 1 — `RentalSigningSnapshot.java`

Create the file with EXACTLY this content:

```java
package com.github.jenkaby.bikerental.rental;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

public record RentalSigningSnapshot(
        Long rentalId,
        Long version,
        UUID customerId,
        Duration plannedDuration,
        BigDecimal estimatedCost,
        List<EquipmentItem> equipments
) {

    public record EquipmentItem(
            Long equipmentId,
            String equipmentUid,
            String equipmentTypeSlug,
            BigDecimal estimatedCost
    ) {
    }
}
```

### File 2 — `RentalNotAwaitingSignatureException.java`

Create the file with EXACTLY this content:

```java
package com.github.jenkaby.bikerental.rental;

import com.github.jenkaby.bikerental.rental.domain.model.RentalStatus;
import lombok.Getter;

@Getter
public class RentalNotAwaitingSignatureException extends RuntimeException {

    private final Long rentalId;
    private final RentalStatus currentStatus;

    public RentalNotAwaitingSignatureException(Long rentalId, RentalStatus currentStatus) {
        super("Rental %d is not awaiting signature. Current status: %s".formatted(rentalId, currentStatus));
        this.rentalId = rentalId;
        this.currentStatus = currentStatus;
    }
}
```

### File 3 — `RentalSigningVersionMismatchException.java`

Create the file with EXACTLY this content:

```java
package com.github.jenkaby.bikerental.rental;

import lombok.Getter;

@Getter
public class RentalSigningVersionMismatchException extends RuntimeException {

    private final Long rentalId;
    private final Long expectedVersion;
    private final Long actualVersion;

    public RentalSigningVersionMismatchException(Long rentalId, Long expectedVersion, Long actualVersion) {
        super("Rental %d version mismatch. Expected: %d, actual: %d".formatted(rentalId, expectedVersion, actualVersion));
        this.rentalId = rentalId;
        this.expectedVersion = expectedVersion;
        this.actualVersion = actualVersion;
    }
}
```

> These exceptions extend `RuntimeException` (not the module-internal `BikeRentalException`) because they are
> part of the cross-module API contract consumed by the agreement module; FR-05 maps them to 409 responses via
> its own advice. Do NOT catalogue error codes here — that happens in FR-05.

## 4. Validation Steps

Execute the following command to ensure this task was successful. Do NOT run the full application server.

```bash
./gradlew :service:compileJava "-Dspring.profiles.active=test"
```

</task_file_template>
