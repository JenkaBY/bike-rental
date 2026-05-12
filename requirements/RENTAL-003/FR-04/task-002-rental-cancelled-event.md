# Task 002: Create RentalCancelled Event and RentalEventMapper Mapping

> **Applied Skill:** `spring-boot-modulith/SKILL.md` — domain events as records implementing
> `BikeRentalEvent`; `java.instructions.md` — Java Records for immutable data

## 1. Objective

Create the `RentalCancelled` domain event record in the shared module (consistent with
`RentalStarted` and `RentalCompleted`), and add a `toRentalCancelled(Rental)` method to
`RentalEventMapper`.

## 2. File to Modify / Create

### File A — RentalCancelled Event

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/shared/domain/event/RentalCancelled.java`
* **Action:** Create New File

```java
package com.github.jenkaby.bikerental.shared.domain.event;

import java.util.List;
import java.util.UUID;

public record RentalCancelled(
        Long rentalId,
        UUID customerId,
        List<Long> equipmentIds
) implements BikeRentalEvent {
}
```

### File B — RentalEventMapper update

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/rental/application/mapper/RentalEventMapper.java`
* **Action:** Modify Existing File

**Imports Required:**

Add to the existing import block:

```java
import com.github.jenkaby.bikerental.shared.domain.event.RentalCancelled;
```

**Code to Add:**

* **Location:** After the existing `RentalUpdated.RentalState toRentalState(Rental source);`
  method declaration at the bottom of the interface, before the closing `}`.

```java
    @Mapping(target = "rentalId", source = "id")
    @Mapping(target = "equipmentIds", source = "equipments")
    RentalCancelled toRentalCancelled(Rental rental);
```

> MapStruct will derive `customerId` directly from `Rental.customerId` (name match).
> `equipmentIds` is mapped via the existing `RentalEquipmentMapper` (same as `toRentalStarted`).

## 4. Validation Steps

skip