# Task 001: Create `EquipmentAvailabilityService`

> **Applied Skill:** `d:\Projects\private\bikerent\.github\skills\spring-boot-modulith\SKILL.md` — Service placed in a
> non-public package inside the rental module so Spring Modulith prevents other modules from injecting it directly.
> `d:\Projects\private\bikerent\.github\skills\spring-boot-data-ddd\SKILL.md` — Thin application service delegating
> to a domain repository port; constructor injection only.

## 1. Objective

Create `EquipmentAvailabilityService` in `rental/application/service/` as a `@Service` bean that wraps
`RentalEquipmentRepository.findOccupiedEquipmentIds`. It short-circuits and returns `Set.of()` when the input is empty
(consistent with the repository adapter). Being in a non-public package, Spring Modulith will reject any cross-module
injection attempt at context startup.

## 2. File to Modify / Create

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/rental/application/service/EquipmentAvailabilityService.java`
* **Action:** Create New File

## 3. Code Implementation

**Imports Required:** See snippet below.

**Code to Add/Replace:**

* **Location:** New file — entire file content below.
* **Snippet:**

```java
package com.github.jenkaby.bikerental.rental.application.service;

import com.github.jenkaby.bikerental.rental.domain.repository.RentalEquipmentRepository;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class EquipmentAvailabilityService {

    private final RentalEquipmentRepository rentalEquipmentRepository;

    public EquipmentAvailabilityService(RentalEquipmentRepository rentalEquipmentRepository) {
        this.rentalEquipmentRepository = rentalEquipmentRepository;
    }

    public Set<Long> getUnavailableIds(Set<Long> equipmentIds) {
        if (equipmentIds.isEmpty()) {
            return Set.of();
        }
        return rentalEquipmentRepository.findOccupiedEquipmentIds(equipmentIds);
    }
}
```

> **Key rules:**
> - The class is `public` so it can be injected by other classes **within** the rental module (e.g., the validator and
    > the use case service). Spring Modulith guards the module boundary at the package level, not the class visibility
    > level — no types from this class may appear in the module's public API (`rental/` root package exposed types).
> - The empty-set guard at the service level is the canonical early-exit point for callers; the repository adapter has
    > an identical guard as a defence-in-depth measure.

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```
