# Task 003: Rewrite `BaseRequestedEquipmentValidator#validateAvailability`

> **Applied Skill:** `d:\Projects\private\bikerent\.github\instructions\java.instructions.md` — Inject new dependency
> via constructor; replace old `isAvailable()` / `status_slug` check with live `EquipmentAvailabilityService` query;
> throw new domain exception `EquipmentOccupiedException`.

## 1. Objective

Replace the body of `BaseRequestedEquipmentValidator.validateAvailability(List<EquipmentInfo>)` to extract equipment IDs
from the input list, call `EquipmentAvailabilityService.getUnavailableIds(Set<Long>)`, and throw
`EquipmentOccupiedException` when any IDs are occupied. The method **signature does not change**. The old
`EquipmentInfo.isAvailable()` / `status_slug` check is removed entirely. `EquipmentAvailabilityService` is added as a
constructor-injected dependency.

## 2. File to Modify / Create

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/rental/application/service/validator/BaseRequestedEquipmentValidator.java`
* **Action:** Modify Existing File

## 3. Code Implementation

**Current file content (for reference):**

```java
package com.github.jenkaby.bikerental.rental.application.service.validator;

import com.github.jenkaby.bikerental.equipment.EquipmentInfo;
import com.github.jenkaby.bikerental.shared.exception.EquipmentNotAvailableException;
import com.github.jenkaby.bikerental.shared.exception.ReferenceNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class BaseRequestedEquipmentValidator implements RequestedEquipmentValidator {

    @Override
    public void validateAvailability(List<EquipmentInfo> equipments) {
        List<EquipmentInfo> notAvailable = equipments.stream().filter(eq -> !eq.isAvailable()).toList();
        if (!CollectionUtils.isEmpty(notAvailable)) {
            throw new EquipmentNotAvailableException(notAvailable.getFirst().id(), notAvailable.getFirst().statusSlug());
        }
    }

    @Override
    public void validateSize(List<Long> requestedEquipmentIds, List<EquipmentInfo> equipments) {
        if (equipments.size() != requestedEquipmentIds.size()) {
            var foundIds = equipments.stream()
                    .map(EquipmentInfo::id)
                    .collect(Collectors.toCollection(ArrayList::new));
            var missingIds = new ArrayList<>(requestedEquipmentIds);
            missingIds.removeAll(foundIds);
            throw new ReferenceNotFoundException("Equipment", missingIds.toString());
        }
    }
}
```

**Replace the ENTIRE file with the following:**

```java
package com.github.jenkaby.bikerental.rental.application.service.validator;

import com.github.jenkaby.bikerental.equipment.EquipmentInfo;
import com.github.jenkaby.bikerental.rental.application.service.EquipmentAvailabilityService;
import com.github.jenkaby.bikerental.rental.domain.exception.EquipmentOccupiedException;
import com.github.jenkaby.bikerental.shared.exception.ReferenceNotFoundException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class BaseRequestedEquipmentValidator implements RequestedEquipmentValidator {

    private final EquipmentAvailabilityService equipmentAvailabilityService;

    public BaseRequestedEquipmentValidator(EquipmentAvailabilityService equipmentAvailabilityService) {
        this.equipmentAvailabilityService = equipmentAvailabilityService;
    }

    @Override
    public void validateAvailability(List<EquipmentInfo> equipments) {
        Set<Long> candidateIds = equipments.stream()
                .map(EquipmentInfo::id)
                .collect(Collectors.toSet());
        Set<Long> unavailableIds = equipmentAvailabilityService.getUnavailableIds(candidateIds);
        if (!unavailableIds.isEmpty()) {
            throw new EquipmentOccupiedException(unavailableIds);
        }
    }

    @Override
    public void validateSize(List<Long> requestedEquipmentIds, List<EquipmentInfo> equipments) {
        if (equipments.size() != requestedEquipmentIds.size()) {
            var foundIds = equipments.stream()
                    .map(EquipmentInfo::id)
                    .collect(Collectors.toCollection(ArrayList::new));
            var missingIds = new ArrayList<>(requestedEquipmentIds);
            missingIds.removeAll(foundIds);
            throw new ReferenceNotFoundException("Equipment", missingIds.toString());
        }
    }
}
```

> **Key changes from the original:**
> - `EquipmentNotAvailableException` import removed; `EquipmentOccupiedException` import added.
> - `EquipmentAvailabilityService` injected via constructor (no-arg constructor removed).
> - `CollectionUtils` import removed (no longer used).
> - `validateSize` is unchanged — copy it verbatim.

## 4. Validation Steps

Execute the following commands to ensure this task was successful. Do NOT run the full application server.

```bash
./gradlew :service:compileJava
```
